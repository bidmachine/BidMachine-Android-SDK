package io.bidmachine;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.adcom.Context;

import java.util.HashSet;
import java.util.Set;

import io.bidmachine.models.IBlockedParams;
import io.bidmachine.models.RequestParams;

public final class BlockedParams
        extends RequestParams<BlockedParams>
        implements IBlockedParams<BlockedParams> {

    private Set<String> blockedDomains;
    private Set<String> blockedCategories;
    private Set<String> blockedApplications;

    void build(Context.Restrictions.Builder builder) {
        if (blockedDomains != null) {
            builder.addAllBadv(blockedDomains);
        }
        if (blockedCategories != null) {
            builder.addAllBcat(blockedCategories);
        }
        if (blockedApplications != null) {
            builder.addAllBapp(blockedApplications);
        }
    }

    @Override
    public BlockedParams addBlockedAdvertiserDomain(String domain) {
        if (blockedDomains == null) {
            blockedDomains = new HashSet<>();
        }
        blockedDomains.add(domain);
        return this;
    }

    @Override
    public BlockedParams addBlockedAdvertiserIABCategory(String category) {
        if (blockedCategories == null) {
            blockedCategories = new HashSet<>();
        }
        blockedCategories.add(category);
        return this;
    }

    @Override
    public BlockedParams addBlockedApplication(String bundleOrPackage) {
        if (blockedApplications == null) {
            blockedApplications = new HashSet<>();
        }
        blockedApplications.add(bundleOrPackage);
        return this;
    }

    @Override
    public void merge(@NonNull BlockedParams instance) {
        blockedDomains = updateList(instance.blockedDomains, blockedDomains);
        blockedCategories = updateList(instance.blockedCategories, blockedCategories);
        blockedApplications = updateList(instance.blockedApplications, blockedApplications);
    }

    private <T> Set<T> updateList(Set<T> input, Set<T> output) {
        if (input != null) {
            if (output == null) {
                output = new HashSet<>(input);
            } else {
                output.addAll(input);
            }
        }
        return output;
    }
}
