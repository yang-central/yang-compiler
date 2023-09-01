package org.yangcentral.yangkit.compiler.plugin.stat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import org.yangcentral.yangkit.base.Yang;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.compiler.YangCompiler;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.compiler.plugin.YangCompilerPlugin;
import org.yangcentral.yangkit.compiler.plugin.YangCompilerPluginParameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class YangStatistics implements YangCompilerPlugin {
    private List<Tag> tags = new ArrayList<>();
    private String output;

    public YangNodeDescription getNodeDescription(SchemaNode  schemaNode){
        if(schemaNode == null){
            return null;
        }
        if(schemaNode instanceof VirtualSchemaNode) {
            return null;
        }
        if(schemaNode.getSchemaPath() == null){
            return null;
        }
        YangNodeDescription nodeDescription = new YangNodeDescription();
        nodeDescription.setPath(schemaNode.getSchemaPath().toString());
        nodeDescription.setDescription(schemaNode.getDescription()==null?"":schemaNode.getDescription().getArgStr());
        nodeDescription.setConfig(schemaNode.isConfig()?"true":"false");
        nodeDescription.setSchemaType(schemaNode.getYangKeyword().getLocalName());
        if(schemaNode instanceof TypedDataNode){
            TypedDataNode typedDataNode = (TypedDataNode) schemaNode;
            nodeDescription.setNodeType(typedDataNode.getType().getArgStr());
        }
        nodeDescription.setModule(schemaNode.getContext().getCurModule().getArgStr());
        nodeDescription.setActive(schemaNode.isActive());
        nodeDescription.setDeviated(schemaNode.isDeviated());
        for(Tag tag:tags) {
            String field = tag.getField();
            FName fName = new FName(field);
            QName keyword = null;
            if(fName.getPrefix() == null){
                keyword = new QName(Yang.NAMESPACE,fName.getLocalName());
            } else {
                YangSchemaContext schemaContext = schemaNode.getContext().getSchemaContext();
                List<Module> matched = schemaContext.getModule(fName.getPrefix());
                if(matched == null || matched.isEmpty()){
                    continue;
                }
                URI namespace = matched.get(0).getMainModule().getNamespace().getUri();
                keyword = new QName(namespace,fName.getLocalName());
            }
            if(tag.getValue() == null || tag.getValue().isEmpty()) {
                List<YangStatement> statements = schemaNode.getSubStatement(keyword);
                if(!statements.isEmpty()) {
                    String arg = statements.get(0).getArgStr();
                    if(arg == null || arg.isEmpty()){
                        arg = "true";
                    }
                    Tag nodeTag = new Tag(tag.getName(), tag.getField());
                    nodeTag.setValue(arg);
                    nodeDescription.addTag(nodeTag);
                }
            } else  {
                YangStatement statement = schemaNode.getSubStatement(keyword, tag.getValue());
                if(statement != null){
                    Tag nodeTag = new Tag(tag.getName(), tag.getField());
                    nodeTag.setValue(tag.getValue());
                    nodeDescription.addTag(nodeTag);
                }
            }
        }
        return nodeDescription;
    }
    public List<YangNodeDescription> getNodeDescriptions(SchemaNodeContainer schemaNodeContainer){
        if(schemaNodeContainer == null){
            return new ArrayList<>();
        }

        List<YangNodeDescription> nodeDescriptions = new ArrayList<>();
        if(schemaNodeContainer instanceof SchemaNode){
            SchemaNode schemaNode = (SchemaNode) schemaNodeContainer;
            YangNodeDescription nodeDescription = getNodeDescription(schemaNode);
            if(nodeDescription != null){
                nodeDescriptions.add(nodeDescription);
            }
        }

        for(SchemaNode schemaNode:schemaNodeContainer.getSchemaNodeChildren()){
            List<Module> modules = schemaNode.getContext().getSchemaContext().getModules();
            Module curModule = schemaNode.getContext().getCurModule().getMainModule();
            if(!modules.contains(curModule)){
                //only include the schema node belongs to modules of schema context
                continue;
            }

            if(schemaNode instanceof SchemaNodeContainer){
                nodeDescriptions.addAll(getNodeDescriptions((SchemaNodeContainer) schemaNode));
            } else {
                YangNodeDescription nodeDescription = getNodeDescription(schemaNode);
                if(nodeDescription != null){
                    nodeDescriptions.add(nodeDescription);
                }
            }
        }
        return nodeDescriptions;
    }
    public List<YangNodeDescription> getYangStatistics(YangSchemaContext schemaContext){
        List<YangNodeDescription> nodeDescriptions = new ArrayList<>();
        if(schemaContext == null){
            return nodeDescriptions;
        }
        for(Module module :schemaContext.getModules()){
            if(module instanceof SubModule){
                continue;
            }
            nodeDescriptions.addAll(getNodeDescriptions(module));
        }
        return nodeDescriptions;
    }

    public SXSSFWorkbook serializeXlsx(YangSchemaContext schemaContext){
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet summary = workbook.createSheet("summary");
        summary.setDisplayGridlines(true);
        summary.setAutobreaks(true);
        summary.setColumnWidth(0,5000);
        summary.setColumnWidth(1,5000);
        SXSSFRow sumFirstRow = summary.createRow(0);
        SXSSFCell totalModules = sumFirstRow.createCell(0);
        totalModules.setCellValue("Total modules:");
        SXSSFCell totalModulesVal = sumFirstRow.createCell(1);
        totalModulesVal.setCellValue(schemaContext.getModules().size());

        SXSSFSheet detail = workbook.createSheet("detail");
        detail.setDisplayGridlines(true);
        detail.setAutobreaks(true);
        detail.setColumnWidth(0,20000);
        detail.setColumnWidth(1,5000);
        detail.setColumnWidth(2,20000);
        detail.setColumnWidth(3,5000);
        detail.setColumnWidth(4,5000);
        detail.setColumnWidth(5,5000);
        detail.setColumnWidth(6,5000);
        detail.setColumnWidth(7,5000);
        int size = tags.size();
        for(int i= 0; i< size;i++) {
            detail.setColumnWidth(8+i,5000);
        }

        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);

        CellStyle desStyle = workbook.createCellStyle();
        desStyle.cloneStyleFrom(style);
        desStyle.setAlignment(HorizontalAlignment.JUSTIFY);

        detail.setDefaultColumnStyle(0, style);
        detail.setDefaultColumnStyle(1, desStyle);
        detail.setDefaultColumnStyle(2, style);
        detail.setDefaultColumnStyle(3, style);
        detail.setDefaultColumnStyle(4, style);
        detail.setDefaultColumnStyle(5, style);
        detail.setDefaultColumnStyle(6, style);
        detail.setDefaultColumnStyle(7, style);
        for(int i= 0; i< size;i++) {
            detail.setDefaultColumnStyle(8+i,style);
        }
        //generate header
        SXSSFRow firstRow = detail.createRow(0);
        SXSSFCell yangpath= firstRow.createCell(0);
        yangpath.setCellValue("Path");
        yangpath.setCellStyle(style);
        SXSSFCell active= firstRow.createCell(1);
        active.setCellValue("Active");
        active.setCellStyle(style);
        SXSSFCell description= firstRow.createCell(2);
        description.setCellValue("Description");
        description.setCellStyle(style);
        SXSSFCell config= firstRow.createCell(3);
        config.setCellValue("Config");
        config.setCellStyle(style);
        SXSSFCell schema = firstRow.createCell(4);
        schema.setCellValue("schema");
        schema.setCellStyle(style);
        SXSSFCell type = firstRow.createCell(5);
        type.setCellValue("type");
        type.setCellStyle(style);
        SXSSFCell moduleHeader = firstRow.createCell(6);
        moduleHeader.setCellValue("module");
        moduleHeader.setCellStyle(style);
        SXSSFCell deviated = firstRow.createCell(7);
        deviated.setCellValue("deviated");
        deviated.setCellStyle(style);
        for(int i= 0; i< size;i++) {
            SXSSFCell tagCell = firstRow.createCell(8+i);
            tagCell.setCellValue(tags.get(i).getName());
            tagCell.setCellStyle(style);
        }

        List<YangNodeDescription> totalPaths = getYangStatistics(schemaContext);


        if (null != totalPaths) {
            int pathSize = totalPaths.size();
            SXSSFRow summarySecRow = summary.createRow(1);
            SXSSFCell totalNodes= summarySecRow.createCell(0);
            totalNodes.setCellValue("Total nodes:");
            SXSSFCell totalNodesVal= summarySecRow.createCell(1);
            totalNodesVal.setCellValue(pathSize);
            for (int i = 0; i < pathSize; i++) {
                YangNodeDescription path = totalPaths.get(i);
                SXSSFRow row = detail.createRow(i + 1);
                SXSSFCell pathCell = row.createCell(0);
                pathCell.setCellValue(path.getPath());
                pathCell.setCellStyle(style);
                SXSSFCell activeCell = row.createCell(1);
                activeCell.setCellValue(path.isActive());
                activeCell.setCellStyle(style);
                SXSSFCell descriptionCell = row.createCell(2);
                String str = path.getDescription();
                if(str.length() >= 32767){
                    str = str.substring(0,32767);
                }
                descriptionCell.setCellValue(str);
                descriptionCell.setCellStyle(desStyle);

                SXSSFCell configCell = row.createCell(3);
                configCell.setCellValue(path.getConfig());
                configCell.setCellStyle(style);

                SXSSFCell schemaCell = row.createCell(4);
                schemaCell.setCellValue(path.getSchemaType());
                schemaCell.setCellStyle(style);

                SXSSFCell typeCell = row.createCell(5);
                typeCell.setCellValue(path.getNodeType());
                typeCell.setCellStyle(style);

                SXSSFCell moduleCell = row.createCell(6);
                moduleCell.setCellValue(path.getModule());
                moduleCell.setCellStyle(style);
                SXSSFCell deviateCell = row.createCell(7);
                deviateCell.setCellValue(path.isDeviated());
                deviateCell.setCellStyle(style);
                for(int j= 0; j< size;j++) {
                    SXSSFCell tagValCell = row.createCell(8+ j);
                    Tag valueTag = path.getTag(tags.get(j).getName());
                    if(valueTag == null){
                        tagValCell.setCellValue("");
                    } else {
                        tagValCell.setCellValue(valueTag.getValue());
                    }

                    tagValCell.setCellStyle(style);
                }
            }
        }
        return workbook;
    }
    @Override
    public void run(YangSchemaContext schemaContext, YangCompiler yangCompiler, List<YangCompilerPluginParameter> parameters) throws YangCompilerException {

        for(YangCompilerPluginParameter parameter: parameters){
            if(parameter.getName().equals("output")){
                output = (String) (parameter.getValue());
            } else {
                tags = (List<Tag>)(parameter.getValue());
            }
        }

        SXSSFWorkbook workbook = serializeXlsx(schemaContext);
        try {
            File out = new File(output);
            if(!out.exists()){
                File parent = out.getParentFile();
                if(!parent.exists()){
                    parent.mkdirs();
                }
            }
            workbook.write(new FileOutputStream(out));
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public YangCompilerPluginParameter getParameter(String name, JsonElement value) throws YangCompilerException {
        if (!name.equals("output") && !name.equals("tag")){
            throw new YangCompilerException("unknown parameter:" + name);
        }
        YangCompilerPluginParameter yangCompilerPluginParameter = new YangCompilerPluginParameter() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Object getValue()  {
                if(name.equals("output")){
                    return value.getAsString();
                } else {
                    List<Tag> tags = new ArrayList<>();
                    JsonArray tagArray = value.getAsJsonArray();
                    int size = tagArray.size();
                    for (int i=0; i< size;i++) {
                        JsonObject tagElement = tagArray.get(i).getAsJsonObject();
                        String name = tagElement.get("name").getAsString();
                        String keyword = tagElement.get("keyword").getAsString();
                        Tag tag = new Tag(name,keyword);
                        if(tagElement.get("value")  != null){
                            tag.setValue(tagElement.get("value").getAsString());
                        }
                        tags.add(tag);
                    }
                    return tags;
                }

            }

        };
        return yangCompilerPluginParameter;
    }
}
