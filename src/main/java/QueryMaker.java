import helper_classes.ColumnData;
import helper_classes.GlobalTableData;
import helper_classes.TableData;
import prestoComm.MetaDataManager;
import prestoComm.PrestoMediator;

import java.util.List;

public class QueryMaker {

    private MetaDataManager metaDataManager;
    private PrestoMediator prestoMediator;

    public QueryMaker(MetaDataManager metaDataManager, PrestoMediator prestoMediator){
        this.metaDataManager = metaDataManager;
        this.prestoMediator = prestoMediator;
    }

    public void globalToLocalQueries(GlobalTableData globalTableData){
        //List<TableData> localTables = metaDataManager.getLocalTablesOfGlobalTable(globalTableData);
        //prestoMediator.getLocalTablesQueries(localTables);
    }
}
