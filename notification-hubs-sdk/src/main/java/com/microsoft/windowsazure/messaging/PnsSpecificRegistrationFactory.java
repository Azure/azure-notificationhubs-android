/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
Apache 2.0 License
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */

package com.microsoft.windowsazure.messaging;

import com.microsoft.windowsazure.messaging.Registration.RegistrationType;

/**
 * Represents a factory which creates Registrations according the PNS supported on device, and also provides some PNS specific utility methods
 */
public final class PnsSpecificRegistrationFactory {
	
	/**
	 * Specifies the SDK registration Type.
	 */
	private static RegistrationType mRegistrationType = RegistrationType.fcmv1;

	/**
	 * Keeps the single instance
	 */
	private static final PnsSpecificRegistrationFactory mInstance = new PnsSpecificRegistrationFactory();
	
	/**
	 * Creates a new instance of PnsSpecificRegistrationFactory
	 * https://developer.amazon.com/public/solutions/devices/kindle-fire/specifications/01-device-and-feature-specifications
	 */
	private PnsSpecificRegistrationFactory() {
		boolean isAmazondevice = android.os.Build.MANUFACTURER.compareToIgnoreCase("Amazon")== 0;
		
		if (isAmazondevice)
		{
			mRegistrationType = RegistrationType.adm;
		}
	}

	/**
	 * Returns the instance of PnsSpecificRegistrationFactory
	 * @return Instance of PnsSpecificRegistrationFactory
	 */
	public static PnsSpecificRegistrationFactory getInstance(){
		return mInstance;
	}

	public void setRegistrationType(RegistrationType type){
		mRegistrationType = type;
	}

	public RegistrationType getRegistrationType() {
		return mRegistrationType;
	}
	
	/**
	 * Creates native registration according the PNS supported on device
	 * @param notificationHubPath The Notification Hub path
	 * @return Native registration
	 */
	public Registration createNativeRegistration(String notificationHubPath){
		return createNativeRegistrationForPlatform(notificationHubPath, mRegistrationType);
	}

	/**
	 * Creates native registration gor the given PNS type
	 * @param notificationHubPath The Notification Hub path
	 * @param registrationType	Platform
	 * @return Native registration
	 */
	public Registration createNativeRegistrationForPlatform(String notificationHubPath, RegistrationType registrationType) {
		switch(registrationType) {
			case gcm:{
				return new GcmNativeRegistration(notificationHubPath);
			}
			case fcm:{
				return new FcmNativeRegistration(notificationHubPath);
			}
			case fcmv1:{
				return new FcmV1NativeRegistration(notificationHubPath);
			}
			case baidu:{
				return new BaiduNativeRegistration(notificationHubPath);
			}
			case adm:{
				return new AdmNativeRegistration(notificationHubPath);
			}
			default:{
				throw new AssertionError("Invalid registration type!");
			}
		}
	}
	
	/**
	 * Creates template registration according the PNS supported on device
	 * TODO: This API needs to be deprecated
	 * @param notificationHubPath The Notification Hub path
	 * @return Template registration
	 */
	public TemplateRegistration createTemplateRegistration(String notificationHubPath){
		return createTemplateRegistrationForPlatform(notificationHubPath, mRegistrationType);
	}

	/**
	 * Creates template registration for a given PNS type
	 * TODO: This API needs to be deprecated
	 * @param notificationHubPath The Notification Hub path
	 * @param registrationType	Platform
	 * @return Template registration
	 */
	public TemplateRegistration createTemplateRegistrationForPlatform(String notificationHubPath, RegistrationType registrationType) {
		switch(registrationType) {
			case gcm:
				return new GcmTemplateRegistration(notificationHubPath);
			case fcm:
				return new FcmTemplateRegistration(notificationHubPath);
			case fcmv1:
				return new FcmV1TemplateRegistration(notificationHubPath);
			case baidu:
				return new BaiduTemplateRegistration(notificationHubPath);
			case adm:
				return new AdmTemplateRegistration(notificationHubPath);
			default:
				throw new AssertionError("Invalid registration type!");
		}
	}
	
	/**
	 * Indicates if a registration xml is a Template Registration
	 * @param xml	The xml to check
	 * @return bool
	 */
	public boolean isTemplateRegistration(String xml){
		return isTemplateRegistrationForPlatform(xml, mRegistrationType);
	}

	/**
	 * Indicates if a registration xml is a Template Registration for the given platform
	 * @param xml	The xml to check
	 * @param registrationType	Platform
	 * @return bool
	 */
	public boolean isTemplateRegistrationForPlatform(String xml, RegistrationType registrationType) {
		String templateRegistrationCustomNode;

		switch(registrationType)
		{
			case gcm:{
				templateRegistrationCustomNode =
						GcmTemplateRegistration.GCM_TEMPLATE_REGISTRATION_CUSTOM_NODE;
				break;
			}
			case fcm:{
				templateRegistrationCustomNode =
						FcmTemplateRegistration.FCM_TEMPLATE_REGISTRATION_CUSTOM_NODE;
				break;
			}
			case fcmv1:{
				templateRegistrationCustomNode =
						FcmV1TemplateRegistration.FCM_V1_TEMPLATE_REGISTRATION_CUSTOM_NODE;
				break;
			}
			case baidu:{
				templateRegistrationCustomNode =
						BaiduTemplateRegistration.BAIDU_TEMPLATE_REGISTRATION_CUSTOM_NODE;
				break;
			}
			case adm:{
				templateRegistrationCustomNode =
						AdmTemplateRegistration.ADM_TEMPLATE_REGISTRATION_CUSTOM_NODE;
				break;
			}
			default:{
				throw new AssertionError("Invalid registration type!");
			}
		}

		return xml.contains("<" + (templateRegistrationCustomNode));
	}
	
	/**
	 * Returns PNS handle field name according the PNS supported on device
	 * @return Returns PNS handle field name
	 */
	public String getPNSHandleFieldName(){
		switch(mRegistrationType)
		{
			case gcm:{
				return GcmNativeRegistration.GCM_HANDLE_NODE;
			}
			case fcm:{
				return FcmNativeRegistration.FCM_HANDLE_NODE;
			}
			case fcmv1:{
				return FcmV1NativeRegistration.FCM_V1_HANDLE_NODE;
			}
			case baidu:{
				return BaiduNativeRegistration.BAIDU_HANDLE_NODE;
			}
			case adm:{
				return AdmNativeRegistration.ADM_HANDLE_NODE;
			}
			default:{
				throw new AssertionError("Invalid registration type!");
			}
		}
	}
	
	/**
	 * Returns API origin value according the PNS supported on device
	 * @return API origin value
	 */
	public String getAPIOrigin(){
		
		switch(mRegistrationType)
		{
			case gcm:{
				return "AndroidSdkGcm";
			}
			case fcm:{
				return "AndroidSdkFcm";
			}
			case fcmv1:{
				return "AndroidSdkFcmV1";
			}
			case baidu:{
				return "AndroidSdkBaidu";
			}
			case adm:{
				return "AndroidSdkAdm";
			}
			default:{
				throw new AssertionError("Invalid registration type!");
			}
		}
	}
}
