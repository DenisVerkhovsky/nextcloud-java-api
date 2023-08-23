package org.aarboard.nextcloud.api.filesharing;

import org.aarboard.nextcloud.api.utils.XMLAnswer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ocs")
public class TagsXMLAnswer extends XMLAnswer {
    @XmlElementWrapper(name = "data")
    @XmlElement(name = "element")
    private List<Tag> tags;

    public List<Tag> getTags() {
        return tags;
    }
}