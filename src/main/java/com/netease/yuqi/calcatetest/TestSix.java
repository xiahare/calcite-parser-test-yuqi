package com.netease.yuqi.calcatetest;/*
 * Author: park.yq@alibaba-inc.com
 * Date: 2019/2/25 下午5:04
 */

import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystemImpl;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.BasicSqlType;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;

/**
 * test for https://issues.apache.org/jira/browse/CALCITE-2336?jql=project%20%3D%20CALCITE%20AND%20text%20~%20%22insert%20error%22
 *
 */
public class TestSix {
	public static void main(String[] args) {
		SchemaPlus rootSchema = Frameworks.createRootSchema(true);
		rootSchema.add("USERS", new AbstractTable() {
			@Override public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
				RelDataTypeFactory.FieldInfoBuilder builder = typeFactory.builder();
				builder.add("age", new BasicSqlType(new RelDataTypeSystemImpl() {}, SqlTypeName.INTEGER));
				// make errors for validator, if columns number doesm't match target table, it won't pass validation.
				// From the errors , the issue was fixed.
				//builder.add("name", new BasicSqlType(new RelDataTypeSystemImpl() {}, SqlTypeName.CHAR));
				return builder.build();
			}
		});

		final FrameworkConfig config = Frameworks.newConfigBuilder()
				.parserConfig(SqlParser.Config.DEFAULT)
				.defaultSchema(rootSchema)
				.build();
		Planner planner = Frameworks.getPlanner(config);

		try {
		SqlNode parse =
				planner.parse("insert into users select *\n"
						+ "from (values (1, 'a'), (2, 'b'), (3, 'c')) as t(x, y)\n"
						+ "where x > 1");


			SqlNode validate = planner.validate(parse);
			
			RelRoot relRoot = planner.rel(validate);
			
			System.out.print(RelOptUtil.toString(relRoot.project()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public static void main(String[] args) {
//
//	}
}
