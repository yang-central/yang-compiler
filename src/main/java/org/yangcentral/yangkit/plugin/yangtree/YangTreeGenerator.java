package org.yangcentral.yangkit.plugin.yangtree;


import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.compiler.YangCompiler;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.ext.AugmentStructure;
import org.yangcentral.yangkit.model.api.stmt.ext.YangData;
import org.yangcentral.yangkit.model.api.stmt.ext.YangDataStructure;
import org.yangcentral.yangkit.plugin.YangCompilerPlugin;
import org.yangcentral.yangkit.plugin.YangCompilerPluginParameter;
import org.yangcentral.yangkit.utils.file.FileUtil;

import java.io.File;
import java.util.*;

public class YangTreeGenerator implements YangCompilerPlugin {
    private int lineLength = 72;
    private boolean expandGrouping = true;
    private String output;
    private static final String SPACE = " ";
    private static final String TWO_SPACES = "  ";
    private static final String OFFSET = "   ";
    private static final String VERTICAL_OFFSET = "|  ";

    public String buildYangTree(Module module){
        StringBuilder sb = new StringBuilder();
        if(module instanceof MainModule){
            sb.append("module:");
        } else {
            sb.append("submodule:");
        }
        sb.append(SPACE);
        sb.append(module.getArgStr());
        sb.append("\n");
        //data nodes
        List<DataDefinition> dataDefs = module.getDataDefChildren();

        //augments
        List<Augment> augments = module.getAugments();

        //rpcs
        List<Rpc> rpcs = module.getRpcs();

        //notifications
        List<Notification> notifications = module.getNotifications();


        List<YangUnknown> yangDataList = module.getUnknowns(
                new QName("urn:ietf:params:xml:ns:yang:ietf-restconf","yang-data"));

        // yang structure extension
        List<YangUnknown> yangStructureList = module.getUnknowns(YangDataStructure.YANG_KEYWORD);

        // augment structure extension
        List<YangUnknown> augmentStructureList = module.getUnknowns(AugmentStructure.YANG_KEYWORD);

        if(!module.getGroupings().isEmpty() && (!expandGrouping || (module.getDataDefChildren().isEmpty() &&
                module.getRpcs().isEmpty() && module.getNotifications().isEmpty()
                && module.getAugments().isEmpty() && yangDataList.isEmpty() && yangStructureList.isEmpty()
                && augmentStructureList.isEmpty()))){
            sb.append(buildGroupingContainer(module,TWO_SPACES));
        }
        //data nodes
        if(!dataDefs.isEmpty()){
            int size = dataDefs.size();
            boolean last = false;
            for(int i =0; i< size;i++){
                if(i == (size -1)){
                    last = true;
                }
                DataDefinition dataDefinition = dataDefs.get(i);
                sb.append(buildYangTree(dataDefinition,last,TWO_SPACES,!expandGrouping));
            }
        }
        //augments
        if(!augments.isEmpty()){
            for(Augment augment:augments){
                sb.append(buildAugmentRepresentation(augment,TWO_SPACES));
                sb.append(buildChildren(augment,TWO_SPACES+TWO_SPACES,!expandGrouping));
            }
        }
        //rpcs
        if(!rpcs.isEmpty()){
            sb.append(TWO_SPACES);
            sb.append("rpcs:\n");
            int size = rpcs.size();
            boolean last = false;
            for(int i =0; i< size;i++){
                if(i== (size-1)){
                    last = true;
                }
                Rpc rpc = rpcs.get(i);
                sb.append(buildYangTree(rpc,last,TWO_SPACES+TWO_SPACES,!expandGrouping));
            }
        }
        //notifications
        if(!notifications.isEmpty()){
            sb.append(TWO_SPACES);
            sb.append("notifications:\n");
            int size = notifications.size();
            boolean last = false;
            for(int i =0; i< size;i++){
                if(i== (size-1)){
                    last = true;
                }
                Notification notification = notifications.get(i);
                sb.append(buildYangTree(notification,last,TWO_SPACES+TWO_SPACES,!expandGrouping));
            }
        }
        //yang data
        if(!yangDataList.isEmpty()){
            for(YangUnknown unknown :yangDataList){
                YangData yangData = (YangData) unknown;
                sb.append(TWO_SPACES);
                sb.append("yang-data");
                sb.append(" ");
                sb.append(yangData.getArgStr());
                sb.append(":\n");
                sb.append(buildChildren(yangData,TWO_SPACES+TWO_SPACES,!expandGrouping));
            }
        }
        //structures
        if(!yangStructureList.isEmpty()){
            for(YangUnknown unknown :yangStructureList){
                YangDataStructure structure = (YangDataStructure) unknown;
                sb.append(TWO_SPACES);
                sb.append("structure");
                sb.append(" ");
                sb.append(structure.getArgStr());
                sb.append(":\n");
                if(!expandGrouping){
                    sb.append(buildGroupingContainer(structure,TWO_SPACES+TWO_SPACES));
                }
                sb.append(buildChildren(structure,TWO_SPACES+TWO_SPACES,!expandGrouping));

            }
        }
        //augment-structures
        if(!augmentStructureList.isEmpty()){
            for(YangUnknown unknown :augmentStructureList){
                AugmentStructure augmentStructure = (AugmentStructure) unknown;
                sb.append(TWO_SPACES);
                sb.append("augment-structure");
                sb.append(" ");
                sb.append(augmentStructure.getArgStr());
                sb.append(":\n");
                sb.append(buildChildren(augmentStructure,TWO_SPACES+TWO_SPACES,!expandGrouping));
            }
        }

        return sb.toString();
    }
    private String buildGroupingContainer(GroupingDefContainer groupingDefContainer,String offSet){
        StringBuilder sb = new StringBuilder();
        //sb.append(offSet);
        List<Grouping> groupings = groupingDefContainer.getGroupings();
        int size = groupings.size();
        for(int i =0; i< size;i++){
            boolean last = false;
            if(i == (size -1)){
                last = true;
            }
            Grouping g = groupings.get(i);
            sb.append(buildGrouping(g,last,offSet));
        }
        return sb.toString();
    }
    private List<SchemaNode> getRealSchemaChildren(SchemaNodeContainer schemaNodeContainer,boolean grouping){
        List<SchemaNode> realSchemaNodeChildren = new ArrayList<>();

        for(SchemaNode schemaChild:schemaNodeContainer.getSchemaNodeChildren()){
            if(schemaChild instanceof Augment){
                Augment augment = (Augment) schemaChild;
                if(schemaNodeContainer instanceof SchemaNode){
                    SchemaNode schemaNodeParent = (SchemaNode) schemaNodeContainer;
                    if(augment.getContext().getNamespace() != null && !augment.getContext().getNamespace()
                            .equals(schemaNodeParent.getContext().getNamespace())){
                        continue;
                    }
                }
                if(!grouping){
                    realSchemaNodeChildren.addAll(getRealSchemaChildren(augment,grouping));
                    continue;
                }
            }
            realSchemaNodeChildren.add(schemaChild);
        }
        return realSchemaNodeChildren;
    }
    private String buildChildren(SchemaNodeContainer schemaNodeContainer,String offSet,boolean grouping){
        StringBuilder sb = new StringBuilder();
        List<SchemaNode> schemaNodeChildren = schemaNodeContainer.getSchemaNodeChildren();
        List<SchemaNode> realSchemaNodeChildren = getRealSchemaChildren(schemaNodeContainer,grouping);

        int size = realSchemaNodeChildren.size();
        for(int i = 0;i < size;i++){
            SchemaNode realSchemaNode = realSchemaNodeChildren.get(i);
            boolean subLast = false;
            if(i == (size -1)){
                subLast = true;
            }
            sb.append(buildYangTree(realSchemaNode,subLast,offSet,grouping));

        }
        return sb.toString();
    }
    private String buildGrouping(Grouping grouping,boolean last,String offset){
        StringBuilder sb = new StringBuilder();
        sb.append(offset);
        sb.append("grouping ");
        sb.append(grouping.getArgStr());
        sb.append(":\n");
        GroupingSchemaNodeContainer groupingSchemaNodeContainer = new GroupingSchemaNodeContainer(grouping);
        sb.append(buildGroupingContainer(grouping,offset+TWO_SPACES));
        sb.append(buildChildren(groupingSchemaNodeContainer,offset+TWO_SPACES,true));
        return sb.toString();
    }
    private String buildYangTree(SchemaNode schemaNode,boolean last,String offSet,boolean grouping){
        StringBuilder sb = new StringBuilder();
        if(!grouping && (schemaNode instanceof Uses)){
            Uses uses = (Uses) schemaNode;
            List<SchemaNode> schemaNodes= uses.getSchemaNodeChildren();
            for(int i =0;i < schemaNodes.size();i++){
                SchemaNode exSchemaNode = schemaNodes.get(i);
                boolean subLast = last;
                if(i != (schemaNodes.size() -1)){
                    subLast = false;
                }
                sb.append(buildYangTree(exSchemaNode,subLast,offSet,grouping));
            }
            return sb.toString();
        }
        if(schemaNode instanceof Augment){
            sb.append(buildAugmentRepresentation((Augment) schemaNode,offSet));
        } else {
            sb.append(buildNodeRepresentation(schemaNode,offSet));
        }
        if((schemaNode instanceof GroupingDefContainer) && grouping){
            sb.append(buildGroupingContainer((GroupingDefContainer) schemaNode,offSet+TWO_SPACES));
        }
        if(schemaNode instanceof SchemaNodeContainer){
            String childOffSet = offSet;
            if(schemaNode instanceof Augment){
                childOffSet = childOffSet.concat(TWO_SPACES);
            } else {
                if(last){
                    childOffSet = childOffSet.concat(OFFSET);
                } else {
                    childOffSet = childOffSet.concat(VERTICAL_OFFSET);
                }
            }

            SchemaNodeContainer schemaNodeContainer;
            if(!grouping){
                schemaNodeContainer = (SchemaNodeContainer) schemaNode;
            } else {
                schemaNodeContainer = new GroupingSchemaNodeContainer(schemaNode);
            }

            sb.append(buildChildren(schemaNodeContainer,childOffSet,grouping));
        }

        return sb.toString();
    }

    private String getStatus(YangStatement yangStatement){
        if(yangStatement instanceof Entity){
            Status status = ((Entity) yangStatement).getEffectiveStatus();
            switch (status){
                case CURRENT:{
                    return "+";
                }
                case DEPRECATED:{
                    return "x";
                }
                case OBSOLETE:{
                    return "o";
                }
            }
        }
        return "";
    }

    private String getFlags(YangStatement yangStatement){
        String flags = "";
        if(yangStatement instanceof SchemaNode){
            SchemaNode schemaNode = (SchemaNode) yangStatement;
            if(schemaNode instanceof Uses){
                flags = "-u";
            } else if((schemaNode instanceof Rpc) || (schemaNode instanceof Action)){
                flags ="-x";
            } else if(schemaNode instanceof Notification){
                flags ="-n";
            } else if(schemaNode instanceof Case){
                flags ="";
            } else if(schemaNode.isConfig()){
                flags = "rw";
            }
            else {
                flags ="ro";
                if(schemaNode.getSchemaTreeType() == SchemaTreeType.INPUTTREE){
                    flags = "-w";
                }
            }

            if(!schemaNode.getSubStatement(new QName("urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount",
                    "mount-point")).isEmpty()){
                flags = "mp";
            }
        } else if(yangStatement instanceof Grouping){
            flags = "rw";
        }

        return flags;
    }

    private String getNodeName(YangStatement yangStatement){
        if(yangStatement instanceof Case){
            return ":("+yangStatement.getArgStr() +")";
        } else if(yangStatement instanceof Choice){
            return " ("+yangStatement.getArgStr() +")";
        } else {
            return " " + yangStatement.getArgStr();
        }
    }

    private String getOpts(YangStatement yangStatement){
        if((yangStatement instanceof Leaf)
        ||(yangStatement instanceof Choice)
        ||(yangStatement instanceof Anydata)
        ||(yangStatement instanceof Anyxml)){
            if(!((SchemaDataNode) yangStatement).isMandatory()){
                return "?";
            }
        }else if(yangStatement instanceof Container){
            if(((Container) yangStatement).isPresence()){
                return "!";
            }
        } else if(yangStatement instanceof MultiInstancesDataNode){
            StringBuilder sb = new StringBuilder("*");
            if(yangStatement instanceof YangList){
                YangList yangList = (YangList) yangStatement;
                if(yangList.getKey() != null){
                    sb.append(" [");
                    sb.append(yangList.getKey().getArgStr());
                    sb.append("]");
                }
            }
            return sb.toString();
        }
        return "";
    }

    private String getType(TypedDataNode typedDataNode){
        if(typedDataNode.getType().getRestriction() instanceof LeafRef){
            LeafRef leafRef = (LeafRef) typedDataNode.getType().getRestriction();
            return "-> "+ leafRef.getEffectivePath().getArgStr();
        } else {
            return typedDataNode.getType().getArgStr();
        }
    }

    private String getFeatures(IfFeatureSupport ifFeatureSupport){
        StringBuilder sb = new StringBuilder();
        List<IfFeature> ifFeatures = ifFeatureSupport.getIfFeatures();
        if(!ifFeatures.isEmpty()){
            sb.append(" {");
            int size = ifFeatures.size();
            for(int i =0; i< size;i++){
                IfFeature ifFeature = ifFeatures.get(i);
                sb.append(ifFeature.getArgStr());
                if(i != (size-1)){
                    sb.append(",");
                }
            }
            sb.append("}?");
        }
        return sb.toString();
    }
    public String buildAugmentRepresentation(Augment augment,String offSet){
        StringBuilder sb = new StringBuilder(offSet);
        int beginIndex = sb.length();
        sb.append("augment");
        sb.append(" ");
        int nameIndex = sb.length();
        sb.append(augment.getArgStr());
        sb.append(":\n");
        if(sb.length() > lineLength){
            String subString = sb.substring(0,lineLength);
            int index = subString.lastIndexOf("/");
            StringBuilder newSb = new StringBuilder();
            String firstLine = sb.substring(0,index);
            newSb.append(firstLine);
            newSb.append("\n");
            newSb.append(offSet);
            for(int i=beginIndex;i <(nameIndex+2);i++){
                newSb.append(" ");
            }
            newSb.append(sb.substring(index));
            return newSb.toString();
        }
        return sb.toString();
    }
    public String buildNodeRepresentation(YangStatement yangStatement,String offSet){
        StringBuilder sb = new StringBuilder(offSet);
        int beginIndex = sb.length();
        //status
        sb.append(getStatus(yangStatement)).append("--");
        //flags
        sb.append(getFlags(yangStatement));
        //name
        String name = getNodeName(yangStatement);
        int nameIndex = sb.length();
        if(name.startsWith(" ")){
            nameIndex++;
        }
        sb.append(name);
        sb.append(getOpts(yangStatement));


        if(yangStatement instanceof TypedDataNode){
            int typeIndex = sb.length();
            String type = getType((TypedDataNode) yangStatement);
            if(type.length() + typeIndex+1 > lineLength){
                //change line
                sb.append("\n");
                sb.append(offSet);
                for(int i=beginIndex;i<nameIndex+2;i++){
                    sb.append(" ");
                }
                sb.append(type);
            } else {
                sb.append(" ");
                sb.append(type);
            }

        }


        if(yangStatement instanceof IfFeatureSupport){
            IfFeatureSupport ifFeatureSupport = (IfFeatureSupport) yangStatement;
            String features = getFeatures(ifFeatureSupport);
            if(features.length() >0){
                int ifFeatureIndex = sb.length();
                int lastCrIndex = sb.lastIndexOf("\n");
                if(lastCrIndex == -1){
                    lastCrIndex = 0;
                }
                if(((ifFeatureIndex - lastCrIndex) + features.length() )> lineLength){
                    //change line
                    sb.append("\n");
                    sb.append(offSet);
                    for(int i=beginIndex;i<(nameIndex+2);i++){
                        sb.append(" ");
                    }
                    sb.append(features);
                } else {
                    sb.append(" ");
                    sb.append(features);
                }
            }

        }
        sb.append("\n");
        return sb.toString();
    }
    @Override
    public YangCompilerPluginParameter getParameter(Properties compilerProps, String name, String value) throws YangCompilerException {
        if(!name.equals("output")&&!name.equals("line-length")
        &&!name.equals("expand-grouping")){
            throw new YangCompilerException("unrecognized parameter:"+ name);
        }
        if(name.equals("output")){
            return YangCompilerPlugin.super.getParameter(compilerProps,name,value);
        }
        YangCompilerPluginParameter yangCompilerPluginParameter = new YangCompilerPluginParameter() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Object getValue()  {

                if(name.equals("line-length")){
                    return new Integer(value);
                }

                if(name.equals("expand-grouping")){
                    return Boolean.valueOf(value);
                }
                return null;
            }

        };
        return yangCompilerPluginParameter;


    }

    @Override
    public void run(YangSchemaContext schemaContext, YangCompiler yangCompiler, List<YangCompilerPluginParameter> parameters) throws YangCompilerException {
        for(YangCompilerPluginParameter parameter:parameters){
            if(parameter.getName().equals("output")){
                output = (String) parameter.getValue();
            } else if(parameter.getName().equals("line-length")){
                lineLength = (int) parameter.getValue();
            } else if(parameter.getName().equals("expand-grouping")){
                expandGrouping = (boolean) parameter.getValue();
            }
        }
        if(output == null){
            throw new YangCompilerException("missing mandatory parameter:output");
        }
        File outputDir = new File(output);
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }

        List<Module> modules = schemaContext.getModules();
        for(Module module:modules){
            String yangTree = buildYangTree(module);
            FileUtil.writeUtf8File(yangTree,new File(outputDir,
                    module.getArgStr()+ (module.getCurRevisionDate().isPresent()?"@"+module.getCurRevisionDate().get():"")
                            +"_tree.txt"));
        }


    }
}
