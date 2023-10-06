package org.aarboard.nextcloud.api.filesharing;

import java.util.List;

import org.aarboard.nextcloud.api.utils.XMLAnswer;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ocs")
public class TagsXMLAnswer extends XMLAnswer {
    @XmlElementWrapper(name = "data")
    @XmlElement(name = "element")
    private List<Tag> tags;

    public List<Tag> getTags() {
        return tags;
    }
}