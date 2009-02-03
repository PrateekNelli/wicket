/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.breadcrumb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.LoadableDetachableModel;


/**
 * A component that renders bread crumbs. By default, it renders a horizontal list from left to
 * right (oldest left) with bread crumb links and a ' / ' as a separator, e.g.
 * 
 * <pre>
 * first / second / third
 * </pre>
 * 
 * <p>
 * Delegates how the bread crumb model works to {@link DefaultBreadCrumbsModel}.
 * </p>
 * <p>
 * Override and provide your own markup file if you want to work with other elements, e.g. uls
 * instead of spans.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class BreadCrumbBar extends Panel implements IBreadCrumbModel
{
	/** Default crumb component. */
	private static final class BreadCrumbComponent extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            Component id
		 * @param separatorMarkup
		 *            markup used as a separator between breadcrumbs
		 * @param index
		 *            The index of the bread crumb
		 * @param breadCrumbModel
		 *            The bread crumb model
		 * @param breadCrumbParticipant
		 *            The bread crumb
		 * @param enableLink
		 *            Whether the link should be enabled
		 */
		public BreadCrumbComponent(String id, String separatorMarkup, int index,
			IBreadCrumbModel breadCrumbModel, final IBreadCrumbParticipant breadCrumbParticipant,
			boolean enableLink)
		{
			super(id);
			add(new Label("sep", (index > 0) ? separatorMarkup : "").setEscapeModelStrings(false)
				.setRenderBodyOnly(true));
			BreadCrumbLink link = new BreadCrumbLink("link", breadCrumbModel)
			{
				private static final long serialVersionUID = 1L;

				protected IBreadCrumbParticipant getParticipant(String componentId)
				{
					return breadCrumbParticipant;
				}
			};
			link.setEnabled(enableLink);
			add(link);
			link.add(new Label("label", breadCrumbParticipant.getTitle()).setRenderBodyOnly(true));
		}
	}

	/**
	 * List view for rendering the bread crumbs.
	 */
	protected class BreadCrumbsListView extends ListView implements IBreadCrumbModelListener
	{
		private static final long serialVersionUID = 1L;

		private transient boolean dirty = false;

		private transient int size;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            Component id
		 */
		public BreadCrumbsListView(String id)
		{
			super(id);
			setReuseItems(false);
			setModel(new LoadableDetachableModel()
			{
				private static final long serialVersionUID = 1L;

				protected Object load()
				{
					// save a copy
					List l = new ArrayList(allBreadCrumbParticipants());
					size = l.size();
					return l;
				}
			});
		}

		/**
		 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener#breadCrumbActivated(org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant,
		 *      org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant)
		 */
		public void breadCrumbActivated(IBreadCrumbParticipant previousParticipant,
			IBreadCrumbParticipant breadCrumbParticipant)
		{
			signalModelChange();
		}

		/**
		 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener#breadCrumbAdded(org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant)
		 */
		public void breadCrumbAdded(IBreadCrumbParticipant breadCrumbParticipant)
		{
		}

		/**
		 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener#breadCrumbRemoved(org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant)
		 */
		public void breadCrumbRemoved(IBreadCrumbParticipant breadCrumbParticipant)
		{
		}

		/**
		 * Signal model change.
		 */
		private void signalModelChange()
		{
			// else let the listview recalculate it's children immediately;
			// it was attached, but it needs to go through that again now
			// as the signaling component attached after this
			getModel().detach();
			super.internalOnAttach();
		}

		/**
		 * @see org.apache.wicket.markup.html.list.ListView#onBeforeRender()
		 */
		protected void onBeforeRender()
		{
			super.onBeforeRender();
			if (dirty)
			{
				dirty = false;
			}
		}

		/**
		 * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem item)
		{
			int index = item.getIndex();
			IBreadCrumbParticipant breadCrumbParticipant = (IBreadCrumbParticipant)item.getModelObject();
			item.add(newBreadCrumbComponent("crumb", index, size, breadCrumbParticipant));
		}
	}

	private static final long serialVersionUID = 1L;

	private final IBreadCrumbModel decorated;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            Component id
	 */
	public BreadCrumbBar(String id)
	{
		super(id);
		decorated = new DefaultBreadCrumbsModel();
		BreadCrumbsListView breadCrumbsListView = new BreadCrumbsListView("crumbs");
		addListener(breadCrumbsListView);
		add(breadCrumbsListView);
	}


	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#addListener(org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener)
	 */
	public void addListener(IBreadCrumbModelListener listener)
	{
		decorated.addListener(listener);
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#allBreadCrumbParticipants()
	 */
	public List allBreadCrumbParticipants()
	{
		return decorated.allBreadCrumbParticipants();
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#getActive()
	 */
	public IBreadCrumbParticipant getActive()
	{
		return decorated.getActive();
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#removeListener(org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener)
	 */
	public void removeListener(IBreadCrumbModelListener listener)
	{
		decorated.removeListener(listener);
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel#setActive(org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant)
	 */
	public void setActive(IBreadCrumbParticipant breadCrumbParticipant)
	{
		decorated.setActive(breadCrumbParticipant);
	}

	/**
	 * Gets whether the current bread crumb should be displayed as a link (e.g. for refreshing) or
	 * as a disabled link (effectively just a label). The latter is the default. Override if you
	 * want different behavior.
	 * 
	 * @return Whether the current bread crumb should be displayed as a link; this method returns
	 *         false
	 */
	protected boolean getEnableLinkToCurrent()
	{
		return false;
	}

	/**
	 * @return markup used as a separator between breadcrumbs. By default <code>/</code> is used,
	 *         but <code>&gt;&gt;</code> is also a popular choice.
	 */
	protected String getSeparatorMarkup()
	{
		return "/";
	}


	/**
	 * Creates a new bread crumb component. That component will be rendered as part of the bread
	 * crumbs list (which is a &lt;ul&gt; &lt;li&gt; structure).
	 * 
	 * @param id
	 *            The component id
	 * @param index
	 *            The index of the bread crumb
	 * @param total
	 *            The total number of bread crumbs in the current model
	 * @param breadCrumbParticipant
	 *            the bread crumb
	 * @return A new bread crumb component
	 */
	protected Component newBreadCrumbComponent(String id, int index, int total,
		IBreadCrumbParticipant breadCrumbParticipant)
	{
		boolean enableLink = getEnableLinkToCurrent() || (index < (total - 1));
		return new BreadCrumbComponent(id, getSeparatorMarkup(), index, this,
			breadCrumbParticipant, enableLink);
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	protected void onDetach()
	{
		super.onDetach();
		for (Iterator i = decorated.allBreadCrumbParticipants().iterator(); i.hasNext();)
		{
			IBreadCrumbParticipant crumb = (IBreadCrumbParticipant)i.next();
			if (crumb instanceof Component)
			{
				((Component)crumb).detach();
			}
			else if (crumb instanceof IDetachable)
			{
				((IDetachable)crumb).detach();
			}
		}
	}
}
