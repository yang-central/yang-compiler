package org.yangcentral.yangkit.compiler.plugin.yangtree;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.util.ArrayList;
import java.util.List;

public class GroupingSchemaNodeContainer implements SchemaNodeContainer, GroupingDefContainer {

    private YangStatement container;

    public GroupingSchemaNodeContainer(YangStatement container) {
        this.container = container;
    }

    @Override
    public List<Grouping> getGroupings() {
        List<Grouping> groupings = new ArrayList<>();
        if(container instanceof GroupingDefContainer){
            groupings.addAll(((GroupingDefContainer) container).getGroupings());
        }
        return groupings;
    }

    @Override
    public Grouping getGrouping(String name) {
        return null;
    }

    @Override
    public List<SchemaNode> getSchemaNodeChildren() {
        List<SchemaNode> schemaNodes = new ArrayList<>();
        if(container instanceof DataDefContainer){
            DataDefContainer dataDefContainer = (DataDefContainer) container;
            for(DataDefinition dataDefinition: dataDefContainer.getDataDefChildren()){
                schemaNodes.add(dataDefinition);
            }
        }

        if(container instanceof ActionContainer){
            ActionContainer actionContainer = (ActionContainer) container;
            for(Action action:actionContainer.getActions()){
                schemaNodes.add(action);
            }
        }

        if(container instanceof NotificationContainer) {
            for(Notification notification: ((NotificationContainer) container).getNotifications()){
                schemaNodes.add(notification);
            }
        }
        if(container instanceof Uses) {
            for(Augment augment: ((Uses) container).getAugments()){
                schemaNodes.add(augment);
            }
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
    public List<SchemaNode> getTreeNodeChildren() {
        return null;
    }

    @Override
    public SchemaNode getTreeNodeChild(QName identifier) {
        return null;
    }

    @Override
    public List<SchemaNode> getEffectiveSchemaNodeChildren(boolean ignoreNamespace) {
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
}
