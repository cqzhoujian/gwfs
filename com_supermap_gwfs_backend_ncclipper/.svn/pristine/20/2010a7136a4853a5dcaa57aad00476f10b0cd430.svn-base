package com.supermap.gwfs.clipper;

import com.supermap.gwfs.clipper.entity.ClipperEntity;
import com.supermap.gwfs.clipper.entity.FileParameter;
import com.supermap.gwfs.clipper.entity.NetcdfData;
import com.supermap.gwfs.clipper.entity.SizeParameter;
import com.supermap.gwfs.clipper.util.Logger;

public class Clip2Netcdf {
	private Logger logger = Logger.getLogger(this.getClass());
	private WriteNetcdf writeNetcdf = null;

	public Clip2Netcdf() {
		writeNetcdf = new WriteNetcdf();
	}

	public boolean startClip(ClipperEntity clipperEntity, SizeParameter sizeParameter, NetcdfData netcdfData, FileParameter fileParameter) {
		boolean flag = false;
		int i = 0;

		if (writeNetcdf.writePlFile(clipperEntity, sizeParameter, netcdfData)) {
			logger.info(fileParameter.getPlFileOutName() + "...写完");
			i++;
		}
		if (writeNetcdf.writeSfc1File(clipperEntity, sizeParameter, netcdfData)) {
			logger.info(fileParameter.getSfc1FileOutName() + "...写完");
			i++;
		}
		if (writeNetcdf.writeSfc2File(clipperEntity, sizeParameter, netcdfData)) {
			logger.info(fileParameter.getSfc2FileOutName() + "...写完");
			i++;
		}
		if (i != 0) {
			flag = true;
		}
		return flag;

	}
}
