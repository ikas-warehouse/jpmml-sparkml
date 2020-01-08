/*
 * Copyright (c) 2016 Villu Ruusmann
 *
 * This file is part of JPMML-SparkML
 *
 * JPMML-SparkML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SparkML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SparkML.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.sparkml.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.ml.feature.OneHotEncoder;
import org.jpmml.converter.BinaryFeature;
import org.jpmml.converter.CategoricalFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.PMMLEncoder;
import org.jpmml.sparkml.FeatureConverter;
import org.jpmml.sparkml.SparkMLEncoder;

public class OneHotEncoderConverter extends FeatureConverter<OneHotEncoder> {

	public OneHotEncoderConverter(OneHotEncoder transformer){
		super(transformer);
	}

	@Override
	public List<Feature> encodeFeatures(SparkMLEncoder encoder){
		OneHotEncoder transformer = getTransformer();

		CategoricalFeature categoricalFeature = (CategoricalFeature)encoder.getOnlyFeature(transformer.getInputCol());

		List<?> values = categoricalFeature.getValues();

		boolean dropLast = true;

		if(transformer.isSet(transformer.dropLast())){
			dropLast = transformer.getDropLast();
		}

		return (List)encodeFeature(encoder, categoricalFeature, values, dropLast);
	}

	static
	public List<BinaryFeature> encodeFeature(PMMLEncoder encoder, Feature feature, List<?> values, boolean dropLast){
		List<BinaryFeature> result = new ArrayList<>();

		if(dropLast){
			values = values.subList(0, values.size() - 1);
		}

		for(Object value : values){
			result.add(new BinaryFeature(encoder, feature, value));
		}

		return result;
	}
}