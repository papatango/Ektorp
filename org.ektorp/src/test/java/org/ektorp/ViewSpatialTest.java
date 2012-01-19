package org.ektorp;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Some very basic tests...need more.
 * @author Paul Torres
 *
 */
public class ViewSpatialTest {
	
	ViewSpatial spatial = new ViewSpatial()
	.dbPath("/somedb/")
	.designDocId("_design/doc")
	.setBoundingBox("-1,-1,1,1");

	@Test(expected=java.lang.IllegalStateException.class)
	public void no_boundingbox_exception() {
		ViewSpatial spatiallocal = new ViewSpatial()
		.dbPath("/somedb/")
		.designDocId("_design/doc");
		spatiallocal.buildQuery();
	}

	
	@Test
	public void stale_ok_parameter_added() {
		String url = spatial
			.staleOk(true)
			.buildQuery();
		assertTrue(contains(url, "&stale=ok"));
	}
	
	@Test
	public void stale_ok_set_to_false() {
		String url = spatial
			.staleOk(true)
			.staleOk(false)
			.buildQuery();
		assertFalse(contains(url, "&stale=ok"));
	}
	
	@Test
	public void count_param_added() {
		String url = spatial
			.setCount(false).setCount(true)
			.buildQuery();
		assertTrue(contains(url, "&count=true"));
	}
	@Test
	public void count_param_removed() {
		String url = spatial
			.setCount(true).setCount(false)
			.buildQuery();
		assertFalse(contains(url, "&count=true"));
	}
	
	@Test
	public void planebounds_added() {
		String url = spatial
			.setPlanebounds("-180,-90,180,90")
			.buildQuery();
		//System.out.println(url);
		assertTrue(contains(url, "&plane_bounds=-180%2C-90%2C180%2C90"));
	}
	@Test
	public void planebounds_removed() {
		String url = spatial
				.setPlanebounds("-180,-90,180,90")
				.setPlanebounds("")
				.buildQuery();
		//System.out.println(url);
		assertFalse(contains(url, "&plane_bounds=-180%2C-90%2C180%2C90"));
	}

	
	private boolean contains(String subject, String s) {
		return subject.indexOf(s) > -1;
	}
	
	@Test(expected=java.lang.IllegalStateException.class)
	public void throw_exception_when_dbName_is_missing() {
		new ViewSpatial()
//			.dbPath("/somedb/")
			.designDocId("_design/doc")
			.buildQuery();
	}
	@Test(expected=java.lang.IllegalStateException.class)
	public void throw_exception_when_designDocId_is_missing() {
		new ViewSpatial()
			.dbPath("/somedb/")
//			.designDocId("_design/doc")
			.buildQuery();
	}
	
}
