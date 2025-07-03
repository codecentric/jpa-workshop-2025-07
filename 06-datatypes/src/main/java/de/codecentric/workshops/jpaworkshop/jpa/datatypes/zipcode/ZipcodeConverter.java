package de.codecentric.workshops.jpaworkshop.jpa.datatypes.zipcode;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

@Converter(autoApply = true)
public class ZipcodeConverter implements AttributeConverter<Zipcode, String> {
	@Override
	public String convertToDatabaseColumn(Zipcode attribute) {
		if (attribute == null) {
			return null;
		} else {
			return attribute.getValue();
		}
	}

	@Override
	public Zipcode convertToEntityAttribute(String dbData) {
		if (StringUtils.hasText(dbData)) {
			return Zipcode.of(dbData);
		} else {
			return null;
		}
	}
}
