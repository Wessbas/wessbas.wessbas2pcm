/***************************************************************************
 * Copyright (c) 2016 the WESSBAS project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/


package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

import de.uka.ipd.sdq.pcm.core.CoreFactory;
import de.uka.ipd.sdq.pcm.core.PCMRandomVariable;
import de.uka.ipd.sdq.pcm.parameter.ParameterFactory;
import de.uka.ipd.sdq.pcm.parameter.VariableCharacterisation;
import de.uka.ipd.sdq.pcm.parameter.VariableCharacterisationType;
import de.uka.ipd.sdq.pcm.parameter.VariableUsage;
import de.uka.ipd.sdq.pcm.repository.DataType;
import de.uka.ipd.sdq.pcm.repository.PrimitiveDataType;
import de.uka.ipd.sdq.pcm.repository.PrimitiveTypeEnum;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.stoex.StoexFactory;
import de.uka.ipd.sdq.stoex.VariableReference;

/**
 * @author voegele
 *
 */
public abstract class AbstractCreator {
	
	/**
	 * create a VariableUsage defining a value for a parameter
	 * 
	 * @param parameterName
	 *            the name of the parameter
	 * @param value
	 *            the value to set
	 * @return the VariableUsage created
	 */
	protected VariableUsage createVariableUsage(String parameterName, String value) {
		VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
		VariableCharacterisation characterisation = ParameterFactory.eINSTANCE
				.createVariableCharacterisation();
		PCMRandomVariable PCMVariable = CoreFactory.eINSTANCE
				.createPCMRandomVariable();
		PCMVariable.setSpecification(value);
		characterisation.setSpecification_VariableCharacterisation(PCMVariable);
		characterisation.setType(VariableCharacterisationType.VALUE);
		usage.getVariableCharacterisation_VariableUsage().add(characterisation);
		VariableReference reference = StoexFactory.eINSTANCE
				.createVariableReference();
		reference.setReferenceName(parameterName);
		usage.setNamedReference__VariableUsage(reference);
		return usage;
	}
	
	protected static PrimitiveDataType getPrimitiveDataType(ResourceSet resourceSet, PrimitiveTypeEnum type) throws IOException {
		PrimitiveDataType requestedType = null;
		Repository dataTypeRepository = getPrimitiveTypeRepository(resourceSet);
		EList<DataType> dataTypes = dataTypeRepository.getDataTypes__Repository();
		for(DataType dataType : dataTypes) {
			if(dataType instanceof PrimitiveDataType)
				if(((PrimitiveDataType)dataType).getType() == type)
					requestedType = (PrimitiveDataType)dataType;
		}
		return requestedType;
	}
	
	protected static Repository getPrimitiveTypeRepository(ResourceSet resourceSet) throws IOException {				
		Resource primitiveTypeResource = resourceSet.getResource(
				URI.createURI("pathmap://PCM_MODELS/PrimitiveTypes.repository"),
				true);
		primitiveTypeResource.load(Collections.EMPTY_MAP);
		EObject rootTypeResource = primitiveTypeResource.getContents().get(0);
		Repository typeRepository = (Repository) rootTypeResource;
		return typeRepository;		
	}
	
	/**
	 * copy the requested PrimitiveDataType to the given PCM Repository and return a reference
	 * @param dataTypeRepository the Repository Object to which the PrimitiveDataType is copied
	 * @param resourceSet the ResourceSet for the Generator
	 * @param type the PrimitiveTypeEnum for which a PrimitiveDataType should be copied
	 * @return the copied PrimitiveDataType
	 * @throws IOException
	 */
	protected static PrimitiveDataType importExternalPrimitiveDataType(Repository dataTypeRepository, ResourceSet resourceSet, PrimitiveTypeEnum type) throws IOException {
		PrimitiveDataType palladioType = null;
		PrimitiveDataType copiedType = null;
		boolean typeAlreadyPresent = false;
		for(DataType dataType : dataTypeRepository.getDataTypes__Repository()) {
			if(dataType instanceof PrimitiveDataType) {
				if (((PrimitiveDataType) dataType).getType() == type) {
					typeAlreadyPresent = true;
					copiedType = (PrimitiveDataType) dataType;
				}
			}
		}
		if(!typeAlreadyPresent) {
			palladioType = getPrimitiveDataType(resourceSet, type);
			copiedType = EcoreUtil.copy(palladioType);
			copiedType.setRepository__DataType(dataTypeRepository);
			dataTypeRepository.getDataTypes__Repository().add(copiedType);
		}
		return copiedType;
	}

	
	/**
	 * @param specification
	 * @return PCMRandomVariable
	 */
	protected PCMRandomVariable createPCMRandomVariable(final String specification) {
		// PCM Random Variable for processingRate
		PCMRandomVariable pcmRandomVariable = CoreFactory.eINSTANCE
				.createPCMRandomVariable();		
		pcmRandomVariable.setSpecification(specification);
		return pcmRandomVariable;
	}

	
}
