package be.vdab.gebruikersbeheer.derden.domain;

import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowProcessObject implements Serializable, Comparable<WorkFlowProcessObject> {

	private String id;
	private String comment;
	private String subject;
	private String requestedFor;
	private String requestedBy;
	private String activityName;
	private String activityDesignId;
	private String type;
	private Ikp ikpNummer;
	private String adminDomainName;

	private Boolean approve;

	private Date requestDate;

	private PersonObject personObject;
	private AdminDomainObject adminDomainObject;

	public String getRequestDate() {
		return convertDate(requestDate);
	}

	public Date getRequestDateAsDate() {
		return requestDate;
	}

	private String convertDate(Date date) {
		if (date == null) return null;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		return ValidateUtils.leadingZero(calendar.get(Calendar.DAY_OF_MONTH)) + "-" + (ValidateUtils.leadingZero(calendar.get(Calendar.MONTH) + 1)) + "-" + calendar.get(Calendar.YEAR) + "";
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(250);
		buf.append("WorkFlowProcessObject\n");
		buf.append("[id=");
		buf.append(id);

		buf.append(", comment=");
		buf.append(comment);

		buf.append(", subject=");
		buf.append(subject);

		buf.append(", requestedFor=");
		buf.append(requestedFor);

		buf.append(", requestedBy=");
		buf.append(requestedBy);

		buf.append(", activityName=");
		buf.append(activityName);

		buf.append(", activityDesignId=");
		buf.append(activityDesignId);

		buf.append(", type=");
		buf.append(type);

		buf.append(", approve=");
		buf.append(approve);

		buf.append(", requestDate=");
		buf.append(requestDate);

		buf.append(", person=");
		if (personObject != null) {
			if (personObject.getDn() != null && StringUtils.isNotEmpty(personObject.getDn().getGlobalId())) {
				buf.append(personObject.getDn().getGlobalId());
			}
			buf.append(" ");
			if (StringUtils.isNotEmpty(personObject.getUserId())) {
				buf.append(personObject.getUserId());
			}
		}

		buf.append(", adminDomainObject=");
		if (adminDomainObject != null) {
			buf.append(adminDomainObject.getDn().getGlobalId());
			buf.append(" ");
			buf.append(adminDomainObject.getIkp());
			buf.append(" ");
			buf.append(adminDomainObject.getName());
		}

		return buf.toString();
	}

	public int compareTo(WorkFlowProcessObject workFlowProcessObject) {
		if (this.requestedBy != null && workFlowProcessObject.requestedBy != null) {
			if (this.requestedBy.equalsIgnoreCase(workFlowProcessObject.requestedBy)) {
				if (this.requestDate != null && workFlowProcessObject.requestDate != null) {
					if (this.requestDate.after(workFlowProcessObject.requestDate)) {
						return -1;
					} else if (this.requestDate.before(workFlowProcessObject.requestDate)) {
						return 1;
					} else {
						return 0;
					}
				} else if (this.requestDate != null) {
					return -1;
				} else if (workFlowProcessObject.requestDate != null) {
					return 1;
				}
			} else {
				return this.requestedBy.compareTo(workFlowProcessObject.requestedBy);
			}
		} else if (this.requestedBy != null) {
			return -1;
		} else if (workFlowProcessObject.requestedBy != null) {
			return 1;
		} else {
			// requestedBy is voor beide null
			if (this.requestDate != null && workFlowProcessObject.requestDate != null) {
				if (this.requestDate.after(workFlowProcessObject.requestDate)) {
					return -1;
				} else if (this.requestDate.before(workFlowProcessObject.requestDate)) {
					return 1;
				} else {
					return 0;
				}
			} else if (this.requestDate != null) {
				return -1;
			} else if (workFlowProcessObject.requestDate != null) {
				return 1;
			}
		}

		return 0;
	}
}
