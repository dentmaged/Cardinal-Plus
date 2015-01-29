package in.twizmwaz.cardinal.module.modules.filter;

import org.jdom2.Element;

public class FilterParser {
    
    private final String name;
    
    public FilterParser(final Element element) {
        this.name = element.getParentElement().getAttributeValue("name");
    }

    public String getName() {
        return name;
    }
}
