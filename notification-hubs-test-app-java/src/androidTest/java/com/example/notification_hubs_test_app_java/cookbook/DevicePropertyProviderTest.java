package com.example.notification_hubs_test_app_java.cookbook;

import android.content.Context;

import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SmallTest
public class DevicePropertyProviderTest {

    private final Context mContext = InstrumentationRegistry.getInstrumentation().getContext();

    /**
     * Provides the means to evaluate a string for compliance with ANH Tag limitations as published
     * here:
     *  https://docs.microsoft.com/en-us/azure/notification-hubs/notification-hubs-tags-segment-push-message#tags
     */
    private static final Pattern VALID_TAG_PATTERN = Pattern.compile("^[a-zA-Z0-9_@#\\.:\\-]{1,120}$");

    @Test
    public void getCountryTag() {
        final String expectedPrefix = "Country_";
        final Pattern VALID_COUNTRY_PATTERN = Pattern.compile("^"+ expectedPrefix + "[a-zA-Z]{2,3}$");

        String countryTag = DevicePropertyProvider.getCountryTag();
        Matcher acceptableTagMatcher = VALID_TAG_PATTERN.matcher(countryTag);
        Matcher acceptableCountryMatcher = VALID_COUNTRY_PATTERN.matcher(countryTag);

        Assert.assertTrue("Country tag must be a valid tag", acceptableTagMatcher.matches());
        Assert.assertTrue("Country tag must start with \"" + expectedPrefix + "\"", countryTag.startsWith(expectedPrefix));
        Assert.assertTrue("Country tag must return a two letter country code, or a three letter region code", acceptableCountryMatcher.matches());
    }

    @Test
    public void getLanguageTag() {
        final String expectedPrefix = "Language_";
        final String languageTerm = "([a-zA-Z0-9]{2,3})";
        final String scriptTerm = "(-[a-zA-Z0-9]{4})?";
        final String regionTerm = "(-(?:[a-zA-Z0-9]{2}|[0-9]{3}))?";
        // There are others terms (variant, extension, and privateuse) that are too obscure to test here unless we hear from customers.
        final Pattern VALID_LANGUAGE_PATTERN = Pattern.compile(
                "^" +
                expectedPrefix +
                languageTerm +
                scriptTerm +
                regionTerm +
                "$");

        String languageTag = DevicePropertyProvider.getLanguageTag();
        Matcher acceptableTagMatcher = VALID_TAG_PATTERN.matcher(languageTag);
        Matcher acceptableLanguageMatcher = VALID_LANGUAGE_PATTERN.matcher(languageTag);

        Assert.assertTrue("Language tag must be a valid tag", acceptableTagMatcher.matches());
        Assert.assertTrue("Language tag must start with \"" + expectedPrefix + "\"", languageTag.startsWith(expectedPrefix));
        Assert.assertTrue("Language tag must comply with RFC5646", acceptableLanguageMatcher.matches());
    }

    @Test
    public void getCarrierTag() {
        final String expectedPrefix = "MobileCarrier_";

        String carrierTag = DevicePropertyProvider.getCarrierTag(mContext);
        Matcher acceptableTagMatcher = VALID_TAG_PATTERN.matcher(carrierTag);

        Assert.assertTrue("Carrier tag must be a valid tag", acceptableTagMatcher.matches());
        Assert.assertTrue("Carrier tag must start with \"" + expectedPrefix + "\"", carrierTag.startsWith(expectedPrefix));
        // At time of authoring, the behavior when a phone has no carrier is undefined. No assertion
        // to make sure there is actually a carrier listed.
    }

    @Test
    public void getOemTag() {
        final String expectedPrefix = "Oem_";

        String oemTag = DevicePropertyProvider.getOemTag(mContext);
        Matcher acceptableTagMatcher = VALID_TAG_PATTERN.matcher(oemTag);

        Assert.assertTrue("OEM tag must be a valid tag.", acceptableTagMatcher.matches());
        Assert.assertTrue("OEM tag must start with \"" + expectedPrefix + "\"", oemTag.startsWith(expectedPrefix));
        Assert.assertTrue("OEM tag must be populated", oemTag.length() > expectedPrefix.length());
    }

    @Test
    public void getScreenSizeTag() {
        final String expectedPrefix = "ScreenSize_";
        final Pattern VALID_SCREEN_SIZE_PATTERN = Pattern.compile("^" + expectedPrefix + "[1-9]\\d*X[1-9]\\d*$");

        String screenSizeTag = DevicePropertyProvider.getScreenSizeTag(mContext);
        Matcher acceptableTagMatcher = VALID_TAG_PATTERN.matcher(screenSizeTag);
        Matcher acceptableScreenSizerMatcher = VALID_SCREEN_SIZE_PATTERN.matcher(screenSizeTag);

        Assert.assertTrue("Screen size tag must be a valid tag.", acceptableTagMatcher.matches());
        Assert.assertTrue("Screen size tag must start with \"" + expectedPrefix + "\"", screenSizeTag.startsWith(expectedPrefix));
        Assert.assertTrue("Screen size must adhere to {width}X{height} without prefixing zeros.", acceptableScreenSizerMatcher.matches());
    }
}