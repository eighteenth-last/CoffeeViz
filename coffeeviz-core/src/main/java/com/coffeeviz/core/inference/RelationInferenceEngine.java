package com.coffeeviz.core.inference;

import com.coffeeviz.core.enums.RelationType;
import com.coffeeviz.core.model.DatabaseModel;
import com.coffeeviz.core.model.ForeignKeyModel;
import com.coffeeviz.core.model.TableModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 关系推断引擎
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class RelationInferenceEngine {
    
    /**
     * 推断表之间的关系
     * 
     * @param model 数据库模型
     */
    public void inferRelations(DatabaseModel model) {
        log.info("开始推断表关系");
        
        // 1. 推断显式外键关系类型
        for (TableModel table : model.getTables()) {
            for (ForeignKeyModel fk : table.getForeignKeys()) {
                RelationType type = inferRelationType(fk, table, model);
                fk.setRelationType(type);
                log.debug("表 {} -> {} 关系类型：{}", fk.getFromTable(), fk.getToTable(), type);
            }
        }
        
        // 2. 识别 N:M 中间表
        identifyJunctionTables(model);
        
        log.info("关系推断完成");
    }
    
    /**
     * 推断关系类型
     * 
     * @param fk 外键
     * @param fromTable 源表
     * @param model 数据库模型
     * @return 关系类型
     */
    private RelationType inferRelationType(ForeignKeyModel fk, TableModel fromTable, DatabaseModel model) {
        // 如果外键列是主键的一部分，通常是 1:1 关系
        if (fromTable.getPrimaryKey() != null) {
            Set<String> pkColumns = new HashSet<>(fromTable.getPrimaryKey().getColumns());
            Set<String> fkColumns = new HashSet<>(fk.getFromColumns());
            
            if (pkColumns.equals(fkColumns)) {
                return RelationType.ONE_TO_ONE;
            }
        }
        
        // 默认为 1:N 关系
        return RelationType.ONE_TO_MANY;
    }
    
    /**
     * 识别中间表（N:M 关系）
     * 
     * @param model 数据库模型
     */
    private void identifyJunctionTables(DatabaseModel model) {
        for (TableModel table : model.getTables()) {
            // 中间表特征：
            // 1. 有且仅有 2 个外键
            // 2. 这两个外键的列组成主键
            if (table.getForeignKeys().size() == 2 && table.getPrimaryKey() != null) {
                Set<String> fkColumns = new HashSet<>();
                for (ForeignKeyModel fk : table.getForeignKeys()) {
                    fkColumns.addAll(fk.getFromColumns());
                }
                
                Set<String> pkColumns = new HashSet<>(table.getPrimaryKey().getColumns());
                
                if (fkColumns.equals(pkColumns)) {
                    table.setTableType("JUNCTION");
                    log.info("识别到中间表：{}", table.getName());
                    
                    // 将两个外键的关系类型设置为 MANY_TO_MANY
                    for (ForeignKeyModel fk : table.getForeignKeys()) {
                        fk.setRelationType(RelationType.MANY_TO_MANY);
                    }
                }
            }
        }
    }
}
