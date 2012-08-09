/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ui.swing.overlay;

import imagej.ImageJ;
import imagej.data.overlay.Overlay;
import imagej.ext.InstantiableException;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.log.LogService;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jhotdraw.draw.Figure;

/**
 * The JHotDraw service manages the bidirectional linkage between ImageJ
 * {@link Overlay}s and JHotDraw {@link Figure}s.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class JHotDrawService extends AbstractService {

	private final ArrayList<JHotDrawAdapter> adapters;

	public JHotDrawService(final ImageJ context,
		final PluginService pluginService, final LogService log)
	{
		super(context);

		// ask the plugin service for the list of available JHotDraw adapters
		adapters = new ArrayList<JHotDrawAdapter>();
		final List<PluginInfo<? extends JHotDrawAdapter>> infos =
			pluginService.getPluginsOfType(JHotDrawAdapter.class);
		for (final PluginInfo<? extends JHotDrawAdapter> info : infos) {
			// instantiate the adapter and add it to the list
			try {
				final JHotDrawAdapter adapter = info.createInstance();
				adapter.setContext(context);
				adapter.setPriority(info.getPriority());
				adapters.add(adapter);
			}
			catch (final InstantiableException exc) {
				if (log != null) log.warn("Failed to load " + info.getClassName(), exc);
			}
		}
		if (log != null) {
			log.info("Found " + adapters.size() + " JHotDraw adapters.");
		}
	}

	/**
	 * Gets the first available adapter for the given overlay.
	 * 
	 * @param overlay the overlay to adapt
	 * @return the highest-priority adapter that supports the overlay
	 */
	public JHotDrawAdapter getAdapter(final Overlay overlay) {
		return getAdapter(overlay, null);
	}

	/**
	 * Gets the first available adapter for the given overlay.
	 * 
	 * @param overlay the overlay to adapt
	 * @param figure the figure to be associated with the overlay
	 * @return the highest-priority adapter that supports the overlay adapted to
	 *         the figure
	 */
	public JHotDrawAdapter getAdapter(final Overlay overlay,
		final Figure figure)
	{
		for (final JHotDrawAdapter adapter : adapters) {
			if (adapter.supports(overlay, figure)) return adapter;
		}
		return null;
	}

	/**
	 * Gets all adapters capable of handling a given overlay.
	 * 
	 * @param overlay the overlay to adapt
	 * @return a collection of all adapters capable of handling the overlay
	 */
	public Collection<JHotDrawAdapter> getAdapters(final Overlay overlay)
	{
		return getAdapters(overlay, null);
	}

	/**
	 * Gets all adapters capable of handling a given overlay/figure combination.
	 * 
	 * @param overlay the overlay to adapt
	 * @param figure the figure to be associated with the overlay
	 * @return collection of valid adapters
	 */
	public Collection<JHotDrawAdapter> getAdapters(final Overlay overlay,
		final Figure figure)
	{
		final ArrayList<JHotDrawAdapter> result =
			new ArrayList<JHotDrawAdapter>();
		for (final JHotDrawAdapter adapter : adapters) {
			if (adapter.supports(overlay, figure)) result.add(adapter);
		}
		return result;
	}

	/** Gets all of the discovered adapters. */
	public Collection<JHotDrawAdapter> getAllAdapters() {
		return Collections.unmodifiableCollection(adapters);
	}

}
