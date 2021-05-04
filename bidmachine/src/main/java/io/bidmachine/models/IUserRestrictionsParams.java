package io.bidmachine.models;

public interface IUserRestrictionsParams<SelfType> {

    /**
     * @param hasConsent    - user has given consent to the processing of personal data related to him or her. https://www.eugdpr.org/
     * @param consentString - GDPR consent string if applicable, complying with the comply with the IAB standard
     *                      <a href="https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/Consent%20string%20and%20vendor%20list%20formats%20v1.1%20Final.md">Consent String Format</a>
     *                      in the <a href="https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework">Transparency and Consent Framework</a> technical specifications
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setConsentConfig(boolean hasConsent, String consentString);

    /**
     * @param subject - Flag indicating if GDPR regulations should be applied. <a href="https://wikipedia.org/wiki/General_Data_Protection_Regulation">The  General Data Protection Regulation (GDPR)</a> is a regulation of the European Union
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setSubjectToGDPR(Boolean subject);

    /**
     * @param coppa - Flag indicating if COPPA regulations should be applied. <a href="https://wikipedia.org/wiki/Children%27s_Online_Privacy_Protection_Act">The Children's Online Privacy Protection Act (COPPA)</a> was established by the U.S. Federal Trade Commission
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setCoppa(Boolean coppa);

    /**
     * @param usPrivacyString - CCPA string if applicable, complying with the comply with the IAB standard
     *                        <a href="https://github.com/InteractiveAdvertisingBureau/USPrivacy/blob/master/CCPA/US%20Privacy%20String.md">CCPA String Format</a>
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setUSPrivacyString(String usPrivacyString);

}