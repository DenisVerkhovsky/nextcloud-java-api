package org.aarboard.nextcloud.api.authentication;

import org.aarboard.nextcloud.api.utils.XMLAnswer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ocs")
public class LoginAnswer extends XMLAnswer {
    @XmlElement(name = "data")
    private Data data;

    public String getToken() {
        if (data != null) {
            return data.apppassword;
        }
        return null;
    }

    public static class Data {
        @XmlElement(name = "apppassword")
        public String apppassword;
    }
}
