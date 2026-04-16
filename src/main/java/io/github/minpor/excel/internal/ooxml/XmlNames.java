package io.github.minpor.excel.internal.ooxml;

import javax.xml.stream.XMLStreamReader;

final class XmlNames {

    private XmlNames() {}

    static boolean isStart(XMLStreamReader r, String local) {
        return r.isStartElement() && local.equals(r.getLocalName());
    }

    static boolean isEnd(XMLStreamReader r, String local) {
        return r.isEndElement() && local.equals(r.getLocalName());
    }

    static String attr(XMLStreamReader r, String localName) {
        for (int i = 0; i < r.getAttributeCount(); i++) {
            if (localName.equals(r.getAttributeLocalName(i))) {
                return r.getAttributeValue(i);
            }
        }
        return null;
    }

    /** Resolves {@code r:id}-style: local name {@code id} with prefix {@code r}. */
    static String relationshipId(XMLStreamReader r) {
        for (int i = 0; i < r.getAttributeCount(); i++) {
            if ("id".equals(r.getAttributeLocalName(i)) && "r".equals(r.getAttributePrefix(i))) {
                return r.getAttributeValue(i);
            }
        }
        return null;
    }
}
