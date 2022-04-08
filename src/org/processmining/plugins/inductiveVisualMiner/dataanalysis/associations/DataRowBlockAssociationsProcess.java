package org.processmining.plugins.inductiveVisualMiner.dataanalysis.associations;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.statisticaltests.association.Associations;

public class DataRowBlockAssociationsProcess<C, P> extends DataRowBlockAbstract<Object, C, P> {

	@Override
	public String getName() {
		return "associations";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_associations };
	}

	@Override
	public List<DataRow<Object>> gather(IvMObjectValues inputs) {
		Associations associations = inputs.get(IvMObject.data_analysis_associations);

		List<DataRow<Object>> result = new ArrayList<>();

		for (int att = 0; att < associations.getNumberOfAttributes(); att++) {
			DisplayType associationValue;
			if (associations.getAssociation(att) != -Double.MAX_VALUE) {
				associationValue = DisplayType.numeric(associations.getAssociation(att));
			} else {
				associationValue = DisplayType.NA();
			}
			result.add(new DataRow<Object>(associationValue, associations.getAttribute(att).getName(), "process",
					AssociationType.association.toString()));
			result.add(new DataRow<Object>(associationValue, "process", associations.getAttribute(att).getName(),
					AssociationType.association.toString()));

			if (associations.getImage(att) != null) {
				Icon icon = associations.getImage(att);
				BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics g = bi.createGraphics();
				icon.paintIcon(null, g, 0, 0);
				g.dispose();

				result.add(new DataRow<Object>(DisplayType.image(bi), associations.getAttribute(att).getName(),
						"process", AssociationType.associationPlot.toString()));
				result.add(new DataRow<Object>(DisplayType.image(bi), "process",
						associations.getAttribute(att).getName(), AssociationType.associationPlot.toString()));
			}

			result.add(new DataRow<Object>(DisplayType.literal("P-NA association"),
					associations.getAttribute(att).getName(), "process",
					AssociationType.associationMeasure.toString()));
			result.add(new DataRow<Object>(DisplayType.literal("P-NA association"), "process",
					associations.getAttribute(att).getName(), AssociationType.associationMeasure.toString()));
		}

		return result;

	}
}