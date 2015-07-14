package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.util.CollectionUtil;

public class SnapshotColumnsLogicOffline extends AbstractSnapshotDatabaseObjectsLogicOffline {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return Column.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes() {
        return new Class[]{
                Column.class,
                Relation.class,
                Schema.class,
                Catalog.class
        };
    }

    protected CollectionUtil.CollectionFilter<? extends DatabaseObject> getDatabaseObjectFilter(SnapshotDatabaseObjectsAction action, Scope scope) {
        final ObjectReference relatedTo = action.relatedTo;

        return new CollectionUtil.CollectionFilter<Column>() {
            @Override
            public boolean include(Column column) {
                if (relatedTo.instanceOf(Column.class)) {
                    return column.getName().equals(relatedTo.objectName);
                } else if (relatedTo.instanceOf(Relation.class)) {
                    ObjectName tableName = column.getName().container;
                    return tableName != null && tableName.equals((relatedTo).objectName);
                } else if (relatedTo.instanceOf(Schema.class)) {
                    ObjectName tableName = column.getName().container;
                    return tableName != null && tableName.container != null && tableName.container.equals((relatedTo.objectName));
                } else if (relatedTo.instanceOf(Catalog.class)) {
                    ObjectName tableName = column.getName().container;
                    if (tableName == null || tableName.container == null) {
                        return false;
                    }
                    ObjectName schemaName = tableName.container;

                    return schemaName.container != null && schemaName.container.equals((relatedTo.objectName));
                } else {
                    throw new UnexpectedLiquibaseException("Unexpected relatedTo type: "+relatedTo.getClass().getName());
                }
            }
        };

    }
}