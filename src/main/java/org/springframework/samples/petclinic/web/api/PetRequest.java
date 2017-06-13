package org.springframework.samples.petclinic.web.api;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.hdiv.services.SecureIdentifiable;
import org.hdiv.services.TrustAssertion;
import org.joda.time.LocalDate;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PetRequest implements SecureIdentifiable<Integer> {

	@TrustAssertion(idFor = Pet.class)
	private Integer id;

	@JsonFormat(pattern = "yyyy/MM/dd")
	private LocalDate birthDate;

	@Size(min = 1)
	private String name;

	@Min(1)
	@TrustAssertion(idFor = PetType.class)
	Integer typeId;

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(final LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(final int typeId) {
		this.typeId = typeId;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	@JsonProperty("isNew")
	public boolean isNew() {
		return id == null;
	}

	@Override
	public String toString() {
		return "PetRequest [id=" + id + ", birthDate=" + birthDate + ", name=" + name + ", typeId=" + typeId + "]";
	}

}
