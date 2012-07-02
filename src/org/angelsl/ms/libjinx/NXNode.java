/*
 * Copyright 2011-2012 angelsl.
 *
 * This file is part of libjinx.
 *
 * libjinx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * libjinx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with libjinx.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your
 * choice, provided that you also meet, for each linked independent module,
 * the terms and conditions of the license of that module. An independent
 * module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version
 * of the library, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version.
 */

package org.angelsl.ms.libjinx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A node in an NX file containing a value of type T.
 * 
 * @param <T>
 *            The type of the contained value
 */
public class NXNode<T> implements Iterable<NXNode<?>> {

	static final Nothing _nothing = new Nothing();

	@Override
	public Iterator<NXNode<?>> iterator() {
		if (_children == null) {
			return new Iterator<NXNode<?>>() {

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public NXNode<?> next() {
					return null;
				}

				@Override
				public void remove() {
					throw new IllegalStateException();
				}
			};
		}
		return _children.values().iterator();
	}

	public Collection<NXNode<?>> values() {
		return _children.values();
	}
	
	/**
	 * A class representing nothing.
	 */
	public static class Nothing {
		Nothing() {
		}
	}

	final String _name;
	T _value;
	final NXNode<?> _parent;
	HashMap<String, NXNode<?>> _children = null;
	final NXFile _file;

	NXNode(final String name, final T value, final NXFile file, final NXNode<?> parent) {
		_name = name;
		_value = value;
		_parent = parent;
		_file = file;
	}

	void addChild(NXNode<?> child) {
		if (_children == null)
			_children = new HashMap<String, NXNode<?>>();
		_children.put(child._name, child);
	}

	/**
	 * Gets the name of this node.
	 * 
	 * @return The name of this node.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Gets the parent node of this node, or null if this is the base node.
	 * 
	 * @return The parent node of this node.
	 */
	public NXNode<?> getParent() {
		return _parent;
	}

	/**
	 * Gets the value contained by this node.
	 * 
	 * @return The contained value.
	 */
	public T getValue() {
		return _value;
	}

	/**
	 * Gets the absolute path of this node.
	 * 
	 * @return The path of this node.
	 */
	public String getPath() {
		StringBuilder sb = new StringBuilder(this._name);
		NXNode<?> n = this;
		while ((n = n._parent) != null)
			sb.insert(0, "/").insert(0, n._name);
		return sb.toString();
	}

	/**
	 * Gets the child with the specified name.
	 * 
	 * @param name
	 *            The name of the child to retrieve.
	 * @return The child node, or null if this node has no such child.
	 */
	public NXNode<?> getChild(String name) {
		return _children == null ? null : _children.get(name);
	}

	/**
	 * Gets the number of children this node is parent to.
	 * 
	 * @return The number of children nodes.
	 */
	public int childCount() {
		return _children == null ? 0 : _children.size();
	}

	/**
	 * Returns true if the passed node is a direct child of this node.
	 * 
	 * @param child
	 *            The node to check
	 * @return true if the passed node is a direct child; false otherwise
	 */
	public boolean hasChild(NXNode<?> child) {
		return _children == null ? false : _children.containsValue(child);
	}

	@Override
	public String toString() {
		if (_value == null)
			return String.format("NXNode@%s:%s:%s", getPath(), "null", "null");
		return String.format("NXNode@%s:%s:%s", getPath(), _value.getClass().getName(), _value);
	}
}
