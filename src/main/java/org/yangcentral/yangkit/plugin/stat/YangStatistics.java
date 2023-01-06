package org.yangcentral.yangkit.plugin.stat;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.plugin.YangCompilerPlugin;
import org.yangcentral.yangkit.plugin.YangCompilerPluginParameter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YangStatistics implements YangCompilerPlugin {

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
            }
        }
        return workbook;
    }
    @Override
    public void run(YangSchemaContext schemaContext, Settings settings, List<YangCompilerPluginParameter> parameters) throws YangCompilerException {

        YangCompilerPluginParameter para = parameters.get(0);
        if(!para.getName().equals("output")){
            throw new YangCompilerException("unknown parameter:" + para.getName());
        }
        String output = (String) para.getValue();
        SXSSFWorkbook workbook = serializeXlsx(schemaContext);
        try {
            workbook.write(new FileOutputStream(output));
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
