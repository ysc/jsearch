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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 搜索结果
 * @author 杨尚川
 */
public class Hits {
    private int hitCount;
    private List<Doc> docs = new ArrayList<>();

    public Hits(int hitCount, List<Doc> docs) {
        this.hitCount = hitCount;
        this.docs = docs;
    }

    public int getHitCount() {
        return hitCount;
    }

    public List<Doc> getDocs() {
        return Collections.unmodifiableList(this.docs);
    }
}
