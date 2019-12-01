/**
 * Copyright © 2014-2016 Paolo Simonetto
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ocotillo.structures;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class RedBlackTreeTest extends BinarySearchTreeTestBase {

    private RedBlackTree.RbNode<DataClass, Integer> rbOne;
    private RedBlackTree.RbNode<DataClass, Integer> rbTwo;
    private RedBlackTree.RbNode<DataClass, Integer> rbThree;
    private RedBlackTree.RbNode<DataClass, Integer> rbFour;
    private RedBlackTree.RbNode<DataClass, Integer> rbFive;
    private RedBlackTree.RbNode<DataClass, Integer> rbSix;
    private RedBlackTree.RbNode<DataClass, Integer> rbSeven;
    private RedBlackTree.RbNode<DataClass, Integer> rbEight;
    private RedBlackTree.RbNode<DataClass, Integer> rbNine;

    @Override
    protected BinarySearchTree<DataClass, Integer> createInstance() {
        return new RedBlackTree<>();
    }

    @Before
    public void buildRbNodes() {
        rbOne = new RedBlackTree.RbNode<>(one);
        rbTwo = new RedBlackTree.RbNode<>(two);
        rbThree = new RedBlackTree.RbNode<>(three);
        rbFour = new RedBlackTree.RbNode<>(four);
        rbFive = new RedBlackTree.RbNode<>(five);
        rbSix = new RedBlackTree.RbNode<>(six);
        rbSeven = new RedBlackTree.RbNode<>(seven);
        rbEight = new RedBlackTree.RbNode<>(eight);
        rbNine = new RedBlackTree.RbNode<>(nine);
    }

    @Test
    public void testInsertions() {
        RedBlackTree<DataClass, Integer> tree = new RedBlackTree<>();

        assertThat(tree.size, is(0));
        assertThat(tree.root, is(nullValue()));

        tree.insert(one);
        assertThat(tree.size, is(1));
        assertThat(tree.root.bstKey(), is(1));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild, is(nullValue()));
        assertThat(tree.root.isBlack(), is(true));

        tree.insert(six);
        assertThat(tree.size, is(2));
        assertThat(tree.root.bstKey(), is(1));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.bstKey(), is(6));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(1));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.isRed(), is(true));

        tree.insert(two);
        assertThat(tree.size, is(3));
        assertThat(tree.root.bstKey(), is(2));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(1));
        assertThat(tree.root.rightChild.bstKey(), is(6));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.isRed(), is(true));

        /* Insert a different value for the same key. Tree should not change but
         * we should increase the three size.
         */
        tree.insert(twoBis);
        assertThat(tree.size, is(4));
        assertThat(tree.root.bstKey(), is(2));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(1));
        assertThat(tree.root.rightChild.bstKey(), is(6));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.isRed(), is(true));

        /* Insert an identical value for the same key. Tree should not change and
         * we should not increase the tree size.
         */
        tree.insert(twoBis);
        assertThat(tree.size, is(4));
        assertThat(tree.root.bstKey(), is(2));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(1));
        assertThat(tree.root.rightChild.bstKey(), is(6));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.isRed(), is(true));

        tree.insert(seven);
        assertThat(tree.size, is(5));
        assertThat(tree.root.bstKey(), is(2));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(1));
        assertThat(tree.root.rightChild.bstKey(), is(6));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(7));
        assertThat(tree.root.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(6));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isRed(), is(true));

        tree.insert(nine);
        assertThat(tree.size, is(6));
        assertThat(tree.root.bstKey(), is(2));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(1));
        assertThat(tree.root.rightChild.bstKey(), is(7));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.rightChild.leftChild.bstKey(), is(6));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(9));
        assertThat(tree.root.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isRed(), is(true));

        tree.insert(five);
        assertThat(tree.size, is(7));
        assertThat(tree.root.bstKey(), is(2));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(1));
        assertThat(tree.root.rightChild.bstKey(), is(7));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.rightChild.leftChild.bstKey(), is(6));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(9));
        assertThat(tree.root.rightChild.isRed(), is(true));
        assertThat(tree.root.rightChild.leftChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.leftChild.leftChild.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.leftChild.parent.bstKey(), is(6));
        assertThat(tree.root.rightChild.leftChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.leftChild.isRed(), is(true));

        tree.insert(four);
        assertThat(tree.size, is(8));
        assertThat(tree.root.bstKey(), is(2));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(1));
        assertThat(tree.root.rightChild.bstKey(), is(7));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.rightChild.leftChild.bstKey(), is(5));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(9));
        assertThat(tree.root.rightChild.isRed(), is(true));
        assertThat(tree.root.rightChild.leftChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.leftChild.leftChild.bstKey(), is(4));
        assertThat(tree.root.rightChild.leftChild.rightChild.bstKey(), is(6));
        assertThat(tree.root.rightChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.leftChild.parent.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.leftChild.rightChild.parent.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild.isRed(), is(true));

        tree.insert(three);
        assertThat(tree.size, is(9));
        assertThat(tree.root.bstKey(), is(5));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(2));
        assertThat(tree.root.rightChild.bstKey(), is(7));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(5));
        assertThat(tree.root.leftChild.leftChild.bstKey(), is(1));
        assertThat(tree.root.leftChild.rightChild.bstKey(), is(4));
        assertThat(tree.root.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild.bstKey(), is(6));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(9));
        assertThat(tree.root.rightChild.isRed(), is(true));
        assertThat(tree.root.leftChild.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.rightChild.leftChild.bstKey(), is(3));
        assertThat(tree.root.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.leftChild.parent.bstKey(), is(4));
        assertThat(tree.root.leftChild.rightChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.leftChild.isRed(), is(true));
    }

    @Test
    public void testDeletions() {
        RedBlackTree<DataClass, Integer> tree = new RedBlackTree<>();
        tree.insert(one);
        tree.insert(six);
        tree.insert(two);
        tree.insert(twoBis);
        tree.insert(twoBis);
        tree.insert(seven);
        tree.insert(nine);
        tree.insert(five);
        tree.insert(four);
        tree.insert(three);

        /* Removing an element with the same key of another one. Size decreases,
         * but the tree structure is not altered.
         */
        tree.delete(two);
        assertThat(tree.size, is(8));
        assertThat(tree.root.bstKey(), is(5));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(2));
        assertThat(tree.root.rightChild.bstKey(), is(7));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(5));
        assertThat(tree.root.leftChild.leftChild.bstKey(), is(1));
        assertThat(tree.root.leftChild.rightChild.bstKey(), is(4));
        assertThat(tree.root.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild.bstKey(), is(6));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(9));
        assertThat(tree.root.rightChild.isRed(), is(true));
        assertThat(tree.root.leftChild.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.rightChild.leftChild.bstKey(), is(3));
        assertThat(tree.root.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.leftChild.parent.bstKey(), is(4));
        assertThat(tree.root.leftChild.rightChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.leftChild.isRed(), is(true));

        /* Removing the last element for a given key of another one. Size decreases,
         * and the tree structure is altered.
         */
        tree.delete(twoBis);
        assertThat(tree.size, is(7));
        assertThat(tree.root.bstKey(), is(5));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(3));
        assertThat(tree.root.rightChild.bstKey(), is(7));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(5));
        assertThat(tree.root.leftChild.leftChild.bstKey(), is(1));
        assertThat(tree.root.leftChild.rightChild.bstKey(), is(4));
        assertThat(tree.root.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild.bstKey(), is(6));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(9));
        assertThat(tree.root.rightChild.isRed(), is(true));
        assertThat(tree.root.leftChild.leftChild.parent.bstKey(), is(3));
        assertThat(tree.root.leftChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.parent.bstKey(), is(3));
        assertThat(tree.root.leftChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isBlack(), is(true));

        tree.delete(six);
        assertThat(tree.size, is(6));
        assertThat(tree.root.bstKey(), is(5));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(3));
        assertThat(tree.root.rightChild.bstKey(), is(7));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(5));
        assertThat(tree.root.leftChild.leftChild.bstKey(), is(1));
        assertThat(tree.root.leftChild.rightChild.bstKey(), is(4));
        assertThat(tree.root.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(9));
        assertThat(tree.root.rightChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.leftChild.parent.bstKey(), is(3));
        assertThat(tree.root.leftChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.parent.bstKey(), is(3));
        assertThat(tree.root.leftChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(7));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isRed(), is(true));

        tree.delete(nine);
        assertThat(tree.size, is(5));
        assertThat(tree.root.bstKey(), is(5));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(3));
        assertThat(tree.root.rightChild.bstKey(), is(7));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(5));
        assertThat(tree.root.leftChild.leftChild.bstKey(), is(1));
        assertThat(tree.root.leftChild.rightChild.bstKey(), is(4));
        assertThat(tree.root.leftChild.isRed(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.leftChild.parent.bstKey(), is(3));
        assertThat(tree.root.leftChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.parent.bstKey(), is(3));
        assertThat(tree.root.leftChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.isBlack(), is(true));

        tree.delete(seven);
        assertThat(tree.size, is(4));
        assertThat(tree.root.bstKey(), is(3));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(1));
        assertThat(tree.root.rightChild.bstKey(), is(5));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(3));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(3));
        assertThat(tree.root.rightChild.leftChild.bstKey(), is(4));
        assertThat(tree.root.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.parent.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.isRed(), is(true));

        tree.delete(one);
        assertThat(tree.size, is(3));
        assertThat(tree.root.bstKey(), is(4));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(3));
        assertThat(tree.root.rightChild.bstKey(), is(5));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(4));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(4));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.isBlack(), is(true));

        tree.delete(five);
        assertThat(tree.size, is(2));
        assertThat(tree.root.bstKey(), is(4));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(3));
        assertThat(tree.root.rightChild, is(nullValue()));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(4));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isRed(), is(true));

        tree.delete(four);
        assertThat(tree.size, is(1));
        assertThat(tree.root.bstKey(), is(3));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild, is(nullValue()));
        assertThat(tree.root.isBlack(), is(true));

        tree.delete(three);
        assertThat(tree.size, is(0));
        assertThat(tree.root, is(nullValue()));
    }

    @Test
    public void testDeleteCase2() {
        RedBlackTree<DataClass, Integer> tree = new RedBlackTree<>();

        tree.root = rbFour;

        rbFour.leftChild = rbTwo;
        rbTwo.parent = rbFour;
        rbFour.rightChild = rbSix;
        rbSix.parent = rbFour;

        rbTwo.leftChild = rbOne;
        rbOne.parent = rbTwo;
        rbTwo.rightChild = rbThree;
        rbThree.parent = rbTwo;

        rbSix.leftChild = rbFive;
        rbFive.parent = rbSix;
        rbSix.rightChild = rbEight;
        rbEight.parent = rbSix;
        rbEight.setRed();

        rbEight.leftChild = rbSeven;
        rbSeven.parent = rbEight;
        rbEight.rightChild = rbNine;
        rbNine.parent = rbEight;

        tree.size = 9;

        tree.delete(four);

        assertThat(tree.size, is(8));
        assertThat(tree.root.bstKey(), is(5));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(2));
        assertThat(tree.root.rightChild.bstKey(), is(8));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(5));
        assertThat(tree.root.leftChild.leftChild.bstKey(), is(1));
        assertThat(tree.root.leftChild.rightChild.bstKey(), is(3));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(5));
        assertThat(tree.root.rightChild.leftChild.bstKey(), is(6));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(9));
        assertThat(tree.root.rightChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.parent.bstKey(), is(8));
        assertThat(tree.root.rightChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild.bstKey(), is(7));
        assertThat(tree.root.rightChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(8));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.rightChild.parent.bstKey(), is(6));
        assertThat(tree.root.rightChild.leftChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild.isRed(), is(true));
    }

    @Test
    public void testDeleteCase3() {
        RedBlackTree<DataClass, Integer> tree = new RedBlackTree<>();

        tree.root = rbTwo;
        rbTwo.leftChild = rbOne;
        rbOne.parent = rbTwo;
        rbTwo.rightChild = rbThree;
        rbThree.parent = rbTwo;
        tree.size = 3;

        tree.delete(one);

        assertThat(tree.size, is(2));
        assertThat(tree.root.bstKey(), is(2));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.bstKey(), is(3));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.isRed(), is(true));
    }

    @Test
    public void testDeleteCase4() {
        RedBlackTree<DataClass, Integer> tree = new RedBlackTree<>();

        tree.root = rbTwo;
        rbTwo.leftChild = rbOne;
        rbOne.parent = rbTwo;
        rbTwo.rightChild = rbFour;
        rbFour.parent = rbTwo;
        rbFour.setRed();
        rbFour.leftChild = rbThree;
        rbThree.parent = rbFour;
        rbFour.rightChild = rbFive;
        rbFive.parent = rbFour;
        tree.size = 5;

        tree.delete(three);

        assertThat(tree.size, is(4));
        assertThat(tree.root.bstKey(), is(2));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(1));
        assertThat(tree.root.rightChild.bstKey(), is(4));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(5));
        assertThat(tree.root.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(4));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isRed(), is(true));
    }

    @Test
    public void testDeleteCase5() {
        RedBlackTree<DataClass, Integer> tree = new RedBlackTree<>();

        tree.root = rbFour;

        rbFour.leftChild = rbTwo;
        rbTwo.parent = rbFour;
        rbFour.rightChild = rbEight;
        rbEight.parent = rbFour;

        rbTwo.leftChild = rbOne;
        rbOne.parent = rbTwo;
        rbTwo.rightChild = rbThree;
        rbThree.parent = rbTwo;

        rbEight.leftChild = rbSix;
        rbSix.parent = rbEight;
        rbSix.setRed();
        rbEight.rightChild = rbNine;
        rbNine.parent = rbEight;

        rbSix.leftChild = rbFive;
        rbFive.parent = rbSix;
        rbSix.rightChild = rbSeven;
        rbSeven.parent = rbSix;

        tree.size = 9;

        tree.delete(one);

        assertThat(tree.size, is(8));
        assertThat(tree.root.bstKey(), is(6));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(4));
        assertThat(tree.root.rightChild.bstKey(), is(8));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(6));
        assertThat(tree.root.leftChild.leftChild.bstKey(), is(2));
        assertThat(tree.root.leftChild.rightChild.bstKey(), is(5));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(6));
        assertThat(tree.root.rightChild.leftChild.bstKey(), is(7));
        assertThat(tree.root.rightChild.rightChild.bstKey(), is(9));
        assertThat(tree.root.rightChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.leftChild.parent.bstKey(), is(4));
        assertThat(tree.root.leftChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.rightChild.bstKey(), is(3));
        assertThat(tree.root.leftChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.parent.bstKey(), is(4));
        assertThat(tree.root.leftChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.leftChild.parent.bstKey(), is(8));
        assertThat(tree.root.rightChild.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.rightChild.parent.bstKey(), is(8));
        assertThat(tree.root.rightChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.leftChild.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.leftChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.leftChild.rightChild.isRed(), is(true));
    }

    @Test
    public void testDeleteCase6() {
        RedBlackTree<DataClass, Integer> tree = new RedBlackTree<>();

        tree.root = rbTwo;
        rbTwo.leftChild = rbOne;
        rbOne.parent = rbTwo;
        rbTwo.rightChild = rbFour;
        rbFour.parent = rbTwo;
        rbFour.leftChild = rbThree;
        rbThree.parent = rbFour;
        rbThree.setRed();
        rbFour.rightChild = rbFive;
        rbFive.parent = rbFour;
        rbFive.setRed();
        tree.size = 5;

        tree.delete(one);

        assertThat(tree.size, is(4));
        assertThat(tree.root.bstKey(), is(4));
        assertThat(tree.root.parent, is(nullValue()));
        assertThat(tree.root.leftChild.bstKey(), is(2));
        assertThat(tree.root.rightChild.bstKey(), is(5));
        assertThat(tree.root.isBlack(), is(true));
        assertThat(tree.root.leftChild.parent.bstKey(), is(4));
        assertThat(tree.root.leftChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.bstKey(), is(3));
        assertThat(tree.root.leftChild.isBlack(), is(true));
        assertThat(tree.root.rightChild.parent.bstKey(), is(4));
        assertThat(tree.root.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.rightChild.isBlack(), is(true));
        assertThat(tree.root.leftChild.rightChild.parent.bstKey(), is(2));
        assertThat(tree.root.leftChild.rightChild.leftChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.rightChild, is(nullValue()));
        assertThat(tree.root.leftChild.rightChild.isRed(), is(true));
    }
}
