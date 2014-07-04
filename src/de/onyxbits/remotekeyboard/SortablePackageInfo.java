package de.onyxbits.remotekeyboard;

/**
 * Wrapper class for sorting packages alphabetically by label.
 * 
 * @author patrick
 * 
 */
class SortablePackageInfo implements Comparable<SortablePackageInfo> {


	public String packageName;
	public String displayName;
	
	public SortablePackageInfo(CharSequence pn, CharSequence dn) {
		packageName=pn.toString();
		displayName=dn.toString();
	}

	@Override
	public int compareTo(SortablePackageInfo another) {
		return displayName.compareTo(another.displayName);
	}

}
