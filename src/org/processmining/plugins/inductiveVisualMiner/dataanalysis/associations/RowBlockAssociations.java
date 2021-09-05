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
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.statisticaltests.association.Associations;

public class RowBlockAssociations<C, P> extends DataRowBlockAbstract<Object, C, P> {

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
			DisplayType attributeType;
			{
				Attribute attribute = associations.getAttribute(att);
				if (attribute.isNumeric()) {
					attributeType = DisplayType.literal("numeric");
				} else if (attribute.isLiteral()) {
					attributeType = DisplayType.literal("literal");
				} else if (attribute.isTime()) {
					attributeType = DisplayType.literal("time");
				} else {
					attributeType = DisplayType.literal("other");
				}
			}

			DisplayType associationValue;
			if (associations.getAssociation(att) != -Double.MAX_VALUE) {
				associationValue = DisplayType.numeric(associations.getAssociation(att));
			} else {
				associationValue = DisplayType.NA();
			}

			DisplayType[] values;
			if (associations.getImage(att) != null) {
				Icon icon = associations.getImage(att);
				BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics g = bi.createGraphics();
				icon.paintIcon(null, g, 0, 0);
				g.dispose();

				values = new DisplayType[] { attributeType, associationValue, DisplayType.image(bi) };
			} else {
				values = new DisplayType[] { attributeType, associationValue };
			}

			result.add(new DataRow<Object>(associations.getAttribute(att).getName(), values));
		}

		return result;

	}
}