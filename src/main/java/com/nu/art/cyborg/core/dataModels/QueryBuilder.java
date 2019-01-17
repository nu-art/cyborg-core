/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nu.art.cyborg.core.dataModels;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.generics.Function;
import com.nu.art.core.tools.ArrayTools;

public class QueryBuilder {

	private Uri uri;
	private String[] selectColumnsClause;
	private String joinClause;
	private String whereClause;
	private String[] whereClauseArgs;
	private String groupByClause;
	private String orderByClause;

	public QueryBuilder setUri(Uri uri) {
		this.uri = uri;
		return this;
	}

	public QueryBuilder setSelectColumnsClause(String... selectColumnsClause) {
		this.selectColumnsClause = selectColumnsClause;
		return this;
	}

	public QueryBuilder setJoinClause(String joinClause) {
		this.joinClause = joinClause;
		return this;
	}

	public QueryBuilder setWhereClause(String whereClause) {
		this.whereClause = whereClause;
		return this;
	}

	public QueryBuilder setWhereClauseArgs(String... whereClauseArgs) {
		this.whereClauseArgs = whereClauseArgs;
		return this;
	}

	public QueryBuilder setGroupByClause(String groupByClause) {
		this.groupByClause = groupByClause;
		return this;
	}

	public QueryBuilder setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
		return this;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public String[] getWhereClauseArgs() {
		return whereClauseArgs;
	}

	public final String buildQuery(String tableName) {
		return buildQuery(tableName, false);
	}

	public final String buildRawQuery(String tableName) {
		return buildQuery(tableName, true);
	}

	@SuppressWarnings("unchecked")
	public <Type> String buildQuery(String tableName, boolean isRaw) {
		String query = "SELECT ";
		if (selectColumnsClause == null || selectColumnsClause.length == 0)
			query += "* ";
		else
			query += ArrayTools.join(new Function<String, String>() {
				@Override
				public String map(String selectClause) {
					return selectClause;
				}
			}, ",", selectColumnsClause) + " ";

		query += "FROM ";
		query += tableName;
		query += " ";

		if (joinClause != null && joinClause.length() > 0) {
			query += joinClause;
		}

		if (whereClause != null && whereClause.length() > 0) {
			query += " ";
			query += "WHERE ";
			query += whereClause;
		}

		if (isRaw && whereClauseArgs != null) {
			query += " ";
			for (Object whereClauseArg : whereClauseArgs) {
				if (!query.contains("?"))
					throw new BadImplementationException("Number of arguments does not match '?' in query");
				String value;
				if (whereClauseArg.getClass().getComponentType() != null)
					value = ArrayTools.join(new Function<Object, String>() {
						@Override
						public String map(Object o) {
							return DatabaseUtils.sqlEscapeString(o.toString());
						}
					}, ",", (Type[]) whereClauseArg);
				else
					value = DatabaseUtils.sqlEscapeString(whereClauseArg.toString());
				String[] strings = query.split("\\?", 2);
				query = strings[0] + value + strings[1];
			}
		}

		if (orderByClause != null && orderByClause.length() > 0) {
			query += " ";
			query += orderByClause;
		}

		query += ";";
		return query;
	}

	public Cursor execute(ContentResolver contentResolver)
		throws QueryException {
		Cursor query;
		try {
			query = contentResolver.query(uri, selectColumnsClause, whereClause, whereClauseArgs, orderByClause);
		} catch (Throwable e) {
			throw new QueryException("Error while executing query: " + buildQuery("TABLE", true), e);
		}

		return query;
	}
}