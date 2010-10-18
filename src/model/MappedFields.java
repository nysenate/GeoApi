package model;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MappedFields {
		Object primary;
		Field primaryField;
		ArrayList<Field> foreigns;
		ArrayList<Field> persistents;
		ArrayList<Field> fields;
		
		public MappedFields() {
			primary = null;
			primaryField = null;
			foreigns = new ArrayList<Field>();
			persistents = new ArrayList<Field>();
			fields = new ArrayList<Field>();
		}

		public Object getPrimary() {
			return primary;
		}

		public Field getPrimaryField() {
			return primaryField;
		}

		public ArrayList<Field> getForeigns() {
			return foreigns;
		}

		public ArrayList<Field> getPersistents() {
			return persistents;
		}

		public ArrayList<Field> getFields() {
			return fields;
		}

		public void setPrimary(Object primary) {
			this.primary = primary;
		}

		public void setPrimaryField(Field primaryField) {
			this.primaryField = primaryField;
		}

		public void setForeigns(ArrayList<Field> foreigns) {
			this.foreigns = foreigns;
		}

		public void setPersistents(ArrayList<Field> persistents) {
			this.persistents = persistents;
		}

		public void setFields(ArrayList<Field> fields) {
			this.fields = fields;
		}

		public void addForeign(Field f) {
			foreigns.add(f);
		}
		public void addPersistent(Field f) {
			persistents.add(f);
		}
		public void addField(Field f) {
			fields.add(f);
		}
		
		public ArrayList<Field> getPersistableFields() {
			ArrayList<Field> ret = new ArrayList<Field>();
			ret.addAll(fields);
			ret.addAll(foreigns);
			if(primaryField != null) {
				ret.add(primaryField);
			}
			return ret;
		}
	}