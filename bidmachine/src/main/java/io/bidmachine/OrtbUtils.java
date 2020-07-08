package io.bidmachine;

import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.ByteString;
import com.explorestack.protobuf.Descriptors;
import com.explorestack.protobuf.Message;
import com.explorestack.protobuf.MessageOrBuilder;
import com.explorestack.protobuf.TextFormat;
import com.explorestack.protobuf.UnknownFieldSet;
import com.explorestack.protobuf.WireFormat;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.adcom.ConnectionType;
import com.explorestack.protobuf.adcom.Context;
import com.explorestack.protobuf.adcom.DeviceType;
import com.explorestack.protobuf.adcom.LocationType;
import com.explorestack.protobuf.adcom.OS;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.bidmachine.core.DeviceInfo;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.protobuf.InitRequest;

import static com.explorestack.protobuf.TextFormat.escapeBytes;
import static io.bidmachine.core.Utils.oneOf;

class OrtbUtils {

    private static LocationType getLocationType(@Nullable Location location) {
        if (location != null) {
            if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
                return LocationType.LOCATION_TYPE_GPS;
            } else if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider())
                    || LocationManager.PASSIVE_PROVIDER.equals(location.getProvider())) {
                return LocationType.LOCATION_TYPE_IP;
            }
        }
        return LocationType.LOCATION_TYPE_INVALID;
    }

    static Context.Geo.Builder locationToGeo(@Nullable Location location, boolean shouldProvideUtc) {
        Context.Geo.Builder builder = Context.Geo.newBuilder();
        locationToGeo(builder, location, shouldProvideUtc);
        return builder;
    }

    static void locationToGeo(@NonNull Context.Geo.Builder builder,
                              @Nullable Location location,
                              boolean shouldProvideUtc) {
        if (shouldProvideUtc) {
            builder.setUtcoffset(Utils.getUtcOffsetMinutes());
        }
        if (location != null) {
            builder.setType(getLocationType(location));
            builder.setLat((float) location.getLatitude());
            builder.setLon((float) location.getLongitude());
            builder.setAccur((int) location.getAccuracy());
            builder.setLastfix(location.getTime());
        }
    }

    static ConnectionType getConnectionType(android.content.Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        ConnectionType connectionType;
        if (info == null) {
            connectionType = ConnectionType.CONNECTION_TYPE_INVALID;
        } else {
            switch (info.getType()) {
                case ConnectivityManager.TYPE_MOBILE: {
                    switch (info.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                            connectionType = ConnectionType.CONNECTION_TYPE_CELLULAR_NETWORK_UNKNOWN;
                            break;
                        case TelephonyManager.NETWORK_TYPE_GSM:
                            connectionType = ConnectionType.CONNECTION_TYPE_CELLULAR_NETWORK_2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                            connectionType = ConnectionType.CONNECTION_TYPE_CELLULAR_NETWORK_3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            connectionType = ConnectionType.CONNECTION_TYPE_CELLULAR_NETWORK_5G;
                            break;
                        default:
                            connectionType = ConnectionType.CONNECTION_TYPE_CELLULAR_NETWORK_4G;
                            break;
                    }
                    break;
                }
                case ConnectivityManager.TYPE_WIFI:
                    connectionType = ConnectionType.CONNECTION_TYPE_WIFI;
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    connectionType = ConnectionType.CONNECTION_TYPE_ETHERNET;
                    break;
                default:
                    connectionType = ConnectionType.CONNECTION_TYPE_INVALID;
            }
        }
        return connectionType;
    }

    static void dump(String key, MessageOrBuilder openrtb) {
        if (Logger.isLoggingEnabled()) {
            Logger.log(key + " dump:\n" + printToString(openrtb));
        }
    }

    static InitRequest obtainInitRequest(@NonNull android.content.Context context,
                                         @NonNull String sellerId,
                                         @Nullable TargetingParams targetingParams,
                                         @NonNull DataRestrictions restrictions) {
        final InitRequest.Builder initRequest = InitRequest.newBuilder();
        final String packageName = context.getPackageName();
        if (packageName != null) {
            initRequest.setBundle(packageName);
        }
        initRequest.setSellerId(sellerId);
        initRequest.setOs(OS.OS_ANDROID);
        initRequest.setOsv(Build.VERSION.RELEASE);
        initRequest.setSdk(BidMachine.NAME);
        initRequest.setSdkver(BidMachine.VERSION);
        initRequest.setIfa(AdvertisingPersonalData.getAdvertisingId(context, !restrictions.canSendIfa()));
        initRequest.setBmIfv(BidMachineImpl.get().obtainIFV(context));

        final DeviceInfo deviceInfo = DeviceInfo.obtain(context);
        initRequest.setDeviceType(deviceInfo.isTablet
                                          ? DeviceType.DEVICE_TYPE_TABLET
                                          : DeviceType.DEVICE_TYPE_PHONE_DEVICE);
        if (restrictions.canSendDeviceInfo()) {
            initRequest.setContype(OrtbUtils.getConnectionType(context));
        }
        if (restrictions.canSendGeoPosition()) {
            final Context.Geo.Builder geoBuilder = Context.Geo.newBuilder();
            if (targetingParams != null) {
                targetingParams.build(geoBuilder);
            }
            OrtbUtils.locationToGeo(geoBuilder,
                                    obtainBestLocation(context, targetingParams != null
                                            ? targetingParams.getDeviceLocation() : null, null),
                                    true);
            initRequest.setGeo(geoBuilder);
        }
        return initRequest.build();
    }

    static Location obtainBestLocation(android.content.Context context, Location first, Location second) {
        Location bestLocation = oneOf(first, second);
        Location locationFromLocationService = Utils.getLocation(context);
        if (locationFromLocationService != null &&
                (bestLocation == null || locationFromLocationService.getTime() >= bestLocation.getTime())) {
            bestLocation = locationFromLocationService;
        }
        return bestLocation;
    }

    static void prepareEvents(@NonNull Map<TrackEventType, List<String>> outMap,
                              @Nullable List<Ad.Event> events) {
        if (events == null || events.size() == 0) {
            return;
        }
        for (Ad.Event event : events) {
            TrackEventType eventType = TrackEventType.fromNumber(event.getTypeValue());
            if (eventType == null) continue;
            addEvent(outMap, eventType, event.getUrl());
        }
    }

    static void addEvent(@NonNull Map<TrackEventType, List<String>> outMap,
                         @NonNull TrackEventType eventType,
                         @Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        List<String> urlList = outMap.get(eventType);
        if (urlList == null) {
            urlList = new ArrayList<>(1);
            outMap.put(eventType, urlList);
        }
        urlList.add(url);
    }

    /*
    Protobuf dump utils
     */

    //TODO: optimize for different packages support
    private static String protoRootPackage = "bidmachine";
    private static String[] protoKnownPackages = {"io.bidmachine", "com.explorestack"};

    private static Printer DEFAULT_PRINTER = new Printer();

    private static String printToString(MessageOrBuilder message) {
        try {
            StringBuilder text = new StringBuilder();
            print(message, text);
            return text.toString();
        } catch (IOException var2) {
            throw new IllegalStateException(var2);
        }
    }

    private static void print(MessageOrBuilder message, Appendable output) throws IOException {
        DEFAULT_PRINTER.print(message, new TextGenerator(output));
    }

    private static final class TextGenerator {
        private final Appendable output;
        private final StringBuilder indent;
        private boolean atStartOfLine;

        private TextGenerator(Appendable output) {
            this.indent = new StringBuilder();
            this.atStartOfLine = true;
            this.output = output;
        }

        void indent() {
            this.indent.append("  ");
        }

        void outdent() {
            int length = this.indent.length();
            if (length == 0) {
                throw new IllegalArgumentException(" Outdent() without matching Indent().");
            } else {
                this.indent.delete(length - 2, length);
            }
        }

        void print(CharSequence text) throws IOException {
            int size = text.length();
            int pos = 0;

            for (int i = 0; i < size; ++i) {
                if (text.charAt(i) == '\n') {
                    this.write(text.subSequence(pos, i + 1));
                    pos = i + 1;
                    this.atStartOfLine = true;
                }
            }

            this.write(text.subSequence(pos, size));
        }

        private void write(CharSequence data) throws IOException {
            if (data.length() != 0) {
                if (this.atStartOfLine) {
                    this.atStartOfLine = false;
                    this.output.append(this.indent);
                }

                this.output.append(data);
            }
        }
    }

    private static final class Printer {
        boolean singleLineMode;
        boolean escapeNonAscii;

        private Printer() {
            this.singleLineMode = false;
            this.escapeNonAscii = true;
        }

        private Printer setSingleLineMode(boolean singleLineMode) {
            this.singleLineMode = singleLineMode;
            return this;
        }

        private Printer setEscapeNonAscii(boolean escapeNonAscii) {
            this.escapeNonAscii = escapeNonAscii;
            return this;
        }

        private void print(MessageOrBuilder message, TextGenerator generator) throws IOException {
            Iterator var3 = message.getAllFields().entrySet().iterator();

            while (var3.hasNext()) {
                Map.Entry<Descriptors.FieldDescriptor, Object> field = (Map.Entry) var3.next();
                this.printField(field.getKey(), field.getValue(), generator);
            }

            this.printUnknownFields(message.getUnknownFields(), generator);
        }

        private void printField(Descriptors.FieldDescriptor field,
                                Object value,
                                TextGenerator generator) throws IOException {
            if (field.isRepeated()) {
                Iterator var4 = ((List) value).iterator();

                while (var4.hasNext()) {
                    Object element = var4.next();
                    this.printSingleField(field, element, generator);
                }
            } else {
                this.printSingleField(field, value, generator);
            }

        }

        private void printSingleField(Descriptors.FieldDescriptor field,
                                      Object value,
                                      TextGenerator generator) throws IOException {
            if (field.isExtension()) {
                generator.print("[");
                if (field.getContainingType().getOptions().getMessageSetWireFormat()
                        && field.getType() == Descriptors.FieldDescriptor.Type.MESSAGE
                        && field.isOptional()
                        && field.getExtensionScope() == field.getMessageType()) {
                    generator.print(field.getMessageType().getFullName());
                } else {
                    generator.print(field.getFullName());
                }

                generator.print("]");
            } else if (field.getType() == Descriptors.FieldDescriptor.Type.GROUP) {
                generator.print(field.getMessageType().getName());
            } else {
                generator.print(field.getName());
            }

            if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                if (this.singleLineMode) {
                    generator.print(" { ");
                } else {
                    generator.print(" {\n");
                    generator.indent();
                }
            } else {
                generator.print(": ");
            }

            if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                StringBuilder tmp = new StringBuilder();
                if (value instanceof Any) {
                    Any any = (Any) value;
                    final String typeUrl = any.getTypeUrl();
                    final String[] splits = typeUrl.split("/");
                    final String type = splits[splits.length - 1];

                    for (String pkg : protoKnownPackages) {
                        try {
                            String className = type.replace(protoRootPackage, pkg);
                            OrtbUtils.print(any.unpack((Class<Message>) Class.forName(className)), tmp);
                            break;
                        } catch (ClassNotFoundException ignore) {
                        }
                    }

                    if (tmp.length() > 0) {
                        generator.indent();
                        generator.print(tmp);
                        generator.outdent();
                        generator.print("}");
                        generator.print("\n");
                    }
                } else {
                    OrtbUtils.print((MessageOrBuilder) value, tmp);
                    if (tmp.length() > 0) {
                        generator.print(tmp);
                        generator.outdent();
                        generator.print("}");
                        generator.print("\n");
                    }
                }
            } else {
                this.printFieldValue(field, value, generator);
                if (this.singleLineMode) {
                    generator.print(" ");
                } else {
                    generator.print("\n");
                }
            }

        }

        private void printFieldValue(Descriptors.FieldDescriptor field,
                                     Object value,
                                     TextGenerator generator) throws IOException {
            switch (field.getType()) {
                case INT32:
                case SINT32:
                case SFIXED32:
                    generator.print(value.toString());
                    break;
                case INT64:
                case SINT64:
                case SFIXED64:
                    generator.print(value.toString());
                    break;
                case BOOL:
                    generator.print(value.toString());
                    break;
                case FLOAT:
                    generator.print(value.toString());
                    break;
                case DOUBLE:
                    generator.print(value.toString());
                    break;
                case UINT32:
                case FIXED32:
                    generator.print(TextFormat.unsignedToString((Integer) value));
                    break;
                case UINT64:
                case FIXED64:
                    generator.print(TextFormat.unsignedToString((Long) value));
                    break;
                case STRING:
                    generator.print("\"");
                    generator.print(this.escapeNonAscii
                                            ? escapeBytes(ByteString.copyFromUtf8((String) value))
                                            : TextFormat.escapeDoubleQuotesAndBackslashes((String) value)
                                                        .replace("\n", "\\n"));
                    generator.print("\"");
                    break;
                case BYTES:
                    generator.print("\"");
                    if (value instanceof ByteString) {
                        generator.print(escapeBytes((ByteString) value));
                    } else {
                        generator.print(escapeBytes((byte[]) value));
                    }

                    generator.print("\"");
                    break;
                case ENUM:
                    generator.print(((Descriptors.EnumValueDescriptor) value).getName());
                    break;
                case MESSAGE:
                case GROUP:
                    this.print((Message) value, generator);
            }

        }

        private void printUnknownFields(UnknownFieldSet unknownFields, TextGenerator generator) throws IOException {

            for (Object o : unknownFields.asMap().entrySet()) {
                Map.Entry<Integer, UnknownFieldSet.Field> entry = (Map.Entry) o;
                int number = entry.getKey();
                UnknownFieldSet.Field field = entry.getValue();
                this.printUnknownField(number, 0, field.getVarintList(), generator);
                this.printUnknownField(number, 5, field.getFixed32List(), generator);
                this.printUnknownField(number, 1, field.getFixed64List(), generator);
                this.printUnknownField(number, 2, field.getLengthDelimitedList(), generator);
                Iterator var7 = field.getGroupList().iterator();

                while (var7.hasNext()) {
                    UnknownFieldSet value = (UnknownFieldSet) var7.next();
                    generator.print(entry.getKey().toString());
                    if (this.singleLineMode) {
                        generator.print(" { ");
                    } else {
                        generator.print(" {\n");
                        generator.indent();
                    }

                    this.printUnknownFields(value, generator);
                    if (this.singleLineMode) {
                        generator.print("} ");
                    } else {
                        generator.outdent();
                        generator.print("}\n");
                    }
                }
            }

        }

        private void printUnknownField(int number,
                                       int wireType,
                                       List<?> values,
                                       TextGenerator generator) throws IOException {
            for (Object value : values) {
                generator.print(String.valueOf(number));
                generator.print(": ");
                printUnknownFieldValue(wireType, value, generator);
                generator.print(this.singleLineMode ? " " : "\n");
            }
        }
    }

    private static void printUnknownFieldValue(int tag, Object value, TextGenerator generator) throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case 0:
                generator.print(unsignedToString((Long) value));
                break;
            case 1:
                generator.print(String.format((Locale) null, "0x%016x", (Long) value));
                break;
            case 2:
                generator.print("\"");
                generator.print(escapeBytes((ByteString) value));
                generator.print("\"");
                break;
            case 3:
                DEFAULT_PRINTER.printUnknownFields((UnknownFieldSet) value, generator);
                break;
            case 4:
            default:
                throw new IllegalArgumentException("Bad tag: " + tag);
            case 5:
                generator.print(String.format((Locale) null, "0x%08x", (Integer) value));
        }

    }

    private static String unsignedToString(long value) {
        return value >= 0L ? Long.toString(value) : BigInteger.valueOf(value & 9223372036854775807L)
                                                              .setBit(63)
                                                              .toString();
    }

}
