package org.yangcentral.yangkit.plugin.yangtree;


import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.schema.SchemaTreeType;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.ext.YangData;
import org.yangcentral.yangkit.plugin.YangCompilerPlugin;
import org.yangcentral.yangkit.plugin.YangCompilerPluginParameter;
import org.yangcentral.yangkit.utils.file.FileUtil;

import java.io.File;
import java.util.*;

public class YangTreeGenerator implements YangCompilerPlugin {
    private int lineLength = 72;
    private boolean expandGrouping = true;
    private String output;
    private static final String OFFSET = "   ";
    private static final String VERTICAL_OFFSET = "|  ";

    public String buildYangTree(Module module){
        StringBuilder sb = new StringBuilder();
        if(module instanceof MainModule){
            sb.append("module:");
        } else {
            sb.append("submodule:");
        }
        sb.append(" ");
        sb.append(module.getArgStr());
        sb.append("\n");
        //data nodes
        List<DataDefinition> dataDefs = module.getDataDefChildren();
        if(!dataDefs.isEmpty()){
            int size = dataDefs.size();
            boolean last = false;
            for(int i =0; i< size;i++){
                if(i == (size -1)){
                    last = true;
                }
                DataDefinition dataDefinition = dataDefs.get(i);
                sb.append(buildYangTree(dataDefinition,last,"  "));
            }
        }
        //augments
        List<Augment> augments = module.getAugments();
        if(!augments.isEmpty()){
            for(Augment augment:augments){
                sb.append(buildAugmentRepresentation(augment,"  "));
                sb.append(buildChildren(augment,"    "));
            }
        }
        //rpcs
        List<Rpc> rpcs = module.getRpcs();
        if(!rpcs.isEmpty()){
            sb.append("  ");
            sb.append("rpcs:\n");
            int size = rpcs.size();
            boolean last = false;
            for(int i =0; i< size;i++){
                if(i== (size-1)){
                    last = true;
                }
                Rpc rpc = rpcs.get(i);
                sb.append(buildYangTree(rpc,last,"    "));
            }
        }
        //notifications
        List<Notification> notifications = module.getNotifications();
        if(!notifications.isEmpty()){
            sb.append("  ");
            sb.append("notifications:\n");
            int size = notifications.size();
            boolean last = false;
            for(int i =0; i< size;i++){
                if(i== (size-1)){
                    last = true;
                }
                Notification notification = notifications.get(i);
                sb.append(buildYangTree(notification,last,"    "));
            }
        }
        if(!module.getGroupings().isEmpty() && (!expandGrouping || (module.getDataDefChildren().isEmpty() &&
                module.getRpcs().isEmpty() && module.getNotifications().isEmpty()
        && module.getAugments().isEmpty()))){
            for(Grouping grouping:module.getGroupings()){
                sb.append("  ");
                sb.append("grouping ");
                sb.append(grouping.getArgStr());
                sb.append(":\n");
                SchemaNodeContainer schemaNodeContainer = new SchemaNodeContainer() {
                    @Override
                    public List<SchemaNode> getSchemaNodeChildren() {
                        List<SchemaNode> schemaNodes = new ArrayList<>();
                        for(DataDefinition dataDefinition: grouping.getDataDefChildren()){
                            schemaNodes.add(dataDefinition);
                        }
                        for(Action action:grouping.getActions()){
                            schemaNodes.add(action);
                        }
                        for(Notification notification: grouping.getNotifications()){
                            schemaNodes.add(notification);
                        }
                        return schemaNodes;
                    }

                    @Override
                    public ValidatorResult addSchemaNodeChild(SchemaNode schemaNode) {
                        return null;
                    }

                    @Override
                    public ValidatorResult addSchemaNodeChildren(List<SchemaNode> schemaNodes) {
                        return null;
                    }

                    @Override
                    public SchemaNode getSchemaNodeChild(QName identifier) {
                        return null;
                    }

                    @Override
                    public DataNode getDataNodeChild(QName identifier) {
                        return null;
                    }

                    @Override
                    public List<DataNode> getDataNodeChildren() {
                        return null;
                    }

                    @Override
                    public void removeSchemaNodeChild(QName identifier) {

                    }

                    @Override
                    public void removeSchemaNodeChild(SchemaNode schemaNode) {

                    }

                    @Override
                    public SchemaNode getMandatoryDescendant() {
                        return null;
                    }
                };
                sb.append(buildChildren(schemaNodeContainer,"    "));
            }
        }
        List<YangUnknown> yangDataList = module.getUnknowns(
                new QName("urn:ietf:params:xml:ns:yang:ietf-restconf","yang-data"));
        if(!yangDataList.isEmpty()){
            for(YangUnknown unknown :yangDataList){
                YangData yangData = (YangData) unknown;
                sb.append("  ");
                sb.append("yang-data");
                sb.append(" ");
                sb.append(yangData.getArgStr());
                sb.append(":\n");
                sb.append(buildChildren(yangData,"    "));
            }
        }
        return sb.toString();
    }

    private String buildChildren(SchemaNodeContainer schemaNodeContainer,String offSet){
        StringBuilder sb = new StringBuilder();
        List<SchemaNode> schemaNodeChildren = schemaNodeContainer.getSchemaNodeChildren();
        List<SchemaNode> realSchemaNodeChildren = new ArrayList<>();
        for(SchemaNode schemaChild:schemaNodeChildren){
            if(schemaChild instanceof Augment){
                continue;
            }
            realSchemaNodeChildren.add(schemaChild);
        }
        int size = realSchemaNodeChildren.size();
        for(int i = 0;i < size;i++){
            SchemaNode realSchemaNode = realSchemaNodeChildren.get(i);
            boolean subLast = false;
            if(i == (size -1)){
                subLast = true;
            }
            sb.append(buildYangTree(realSchemaNode,subLast,offSet));

        }
        return sb.toString();
    }
    private String buildYangTree(SchemaNode schemaNode,boolean last,String offSet){
        StringBuilder sb = new StringBuilder();
        if((schemaNode instanceof Uses) && expandGrouping){
            Uses uses = (Uses) schemaNode;
            List<SchemaNode> schemaNodes = uses.getSchemaNodeChildren();
            if(schemaNodes.isEmpty()){
                schemaNodes = new ArrayList<>();
                if(uses.getRefGrouping() != null){

                    for(DataDefinition dataDefinition:uses.getRefGrouping().getDataDefChildren()){
                        schemaNodes.add(dataDefinition);
                    }
                    for(Action action:uses.getRefGrouping().getActions()){
                        schemaNodes.add(action);
                    }
                    for(Notification notification:uses.getRefGrouping().getNotifications()){
                        schemaNodes.add(notification);
                    }
                }
            }
            for(int i =0;i < schemaNodes.size();i++){
                SchemaNode exSchemaNode = schemaNodes.get(i);
                boolean subLast = last;
                if(i != (schemaNodes.size() -1)){
                    subLast = false;
                }
                sb.append(buildYangTree(exSchemaNode,subLast,offSet));
            }
            return sb.toString();
        } else {
            sb.append(buildNodeRepresentation(schemaNode,offSet));
            if(schemaNode instanceof Uses){
                return sb.toString();
            }
        }

        if(schemaNode instanceof SchemaNodeContainer){
            String childOffSet = offSet;
            if(last){
                childOffSet = childOffSet.concat(OFFSET);
            } else {
                childOffSet = childOffSet.concat(VERTICAL_OFFSET);
            }
            sb.append(buildChildren((SchemaNodeContainer) schemaNode,childOffSet));
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
        YangCompilerPluginParameter yangCompilerPluginParameter = new YangCompilerPluginParameter() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Object getValue()  {
                if(name.equals("output")){
                    Iterator<Map.Entry<Object,Object>> it = compilerProps.entrySet().iterator();
                    String formatStr = value;
                    while (it.hasNext()){
                        Map.Entry<Object,Object> entry = it.next();
                        formatStr = formatStr.replaceAll("\\{"+entry.getKey()+"\\}", (String) entry.getValue());
                    }
                    return formatStr;
                }
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
    public void run(YangSchemaContext schemaContext, Settings settings, List<YangCompilerPluginParameter> parameters) throws YangCompilerException {
        for(YangCompilerPluginParameter parameter:parameters){
            if(parameter.getName().equals("output")){
                output = (String) parameter.getValue();
            } else if(parameter.getName().equals("line-length")){
                lineLength = (int) parameter.getValue();
            } else if(parameter.getName().equals("expand-grouping")){
                expandGrouping = (boolean) parameter.getValue();
            }
            if(output == null){
                throw new YangCompilerException("missing mandatory parameter:output");
            }
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
