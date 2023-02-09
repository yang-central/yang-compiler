package org.yangcentral.yangkit.plugin.yangpackage;

import java.util.AbstractMap;
import java.util.Map;

public class PackageInfo extends SubComponetInfo{
    public PackageInfo(String name, String revision) {
        super(name, revision);
    }

    @Override
    protected Map.Entry<String, String> serializeRevision() {
        if(this.getRevision()== null){
            return null;
        }
        Map.Entry<String,String> revisionEntry = new AbstractMap.SimpleEntry<>("version",this.getRevision());
        return revisionEntry;
    }
}
