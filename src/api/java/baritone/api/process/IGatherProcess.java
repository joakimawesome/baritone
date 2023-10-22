/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.api.process;

public interface IGatherProcess extends IBaritoneProcess {
//    /**
//     * Begin to gather crafting materials at nearby cluster.
//     *
//     * @param type The type of material to gather from cluster
//     * @param name The name of the material's source
//     */
//    void gatherByName(MaterialType type, String name);

    /**
     * Begin to gather crafting material at nearby cluster.
     *
     * @param type  The type of material to gather from cluster
     * @param level The material's level requirement
     */
     void gather(String type, String level);

//     /**
//     * Begin gathering nearby cluster.
//     */
//    default void gather() {gather(null, -1);}
    /**
     * Begin gathering designated material.
     *
     * @param type The type of material to gather from cluster
     * @param level The
     */
    default void gather(String type, String level) {gather(type, level);}

//    /**
//     *
//     * @param type
//     * @param name
//     */
//    default void gatherByName(MaterialType type, String name) {gatherByName(type, name);}
}
