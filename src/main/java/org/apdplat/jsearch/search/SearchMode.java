/*
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.apdplat.jsearch.search;

import java.util.Set;

/**
 * 搜索模式
 * @author 杨尚川
 */
public enum SearchMode{
    INTERSECTION, UNION;

    /**
     * 求 existentDocs 和 increasedDocs 的交集，合并结果存储于 existentDocs 中
     * @param existentDocs
     * @param increasedDocs
     */
    public static void intersection(Set<Doc> existentDocs, Set<Doc> increasedDocs){
        existentDocs.parallelStream().forEach(existentDoc -> {
            if (!increasedDocs.contains(existentDoc)) {
                existentDocs.remove(existentDoc);
                return;
            }
            //合并DOC
            for(Doc increasedDoc : increasedDocs){
                if (existentDoc.getId() == increasedDoc.getId()) {
                    existentDoc.merge(increasedDoc);
                    break;
                }
            }
        });
    }

    /**
     * 求 existentDocs 和 increasedDocs 的并集，合并结果存储于 existentDocs 中
     * @param existentDocs
     * @param increasedDocs
     */
    public static void union(Set<Doc> existentDocs, Set<Doc> increasedDocs){
        increasedDocs.parallelStream().forEach(increasedDoc -> {
            if (!existentDocs.contains(increasedDoc)) {
                existentDocs.add(increasedDoc);
            }
        });
    }
}
