package org.cloudbus.cloudsim.ex.mapreduce.models.request;

import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.ex.mapreduce.MapReduceEngine;
import org.cloudbus.cloudsim.ex.mapreduce.models.cloud.Cloud;
import org.cloudbus.cloudsim.ex.mapreduce.models.cloud.DataSource;
import org.cloudbus.cloudsim.ex.mapreduce.models.cloud.VmInstance;
import org.cloudbus.cloudsim.ex.mapreduce.models.cloud.VmType;
import org.cloudbus.cloudsim.ex.mapreduce.models.request.*;
import org.cloudbus.cloudsim.ex.util.Id;

public class MapTask extends Task {

	public List<String> dataSources;
	public String selectedDataSourceName;
	// <Reduce Task ID, IDSize>
	public Map<String, Integer> intermediateData;

	public MapTask(String name, String description, int dSize, int mipb, List<String> dataSources, Map<String, Integer> intermediateData) {
		super(name, description, dSize, mipb);

		this.dataSources = dataSources;
		this.intermediateData = intermediateData;
	}

	public double predictFileTransferTimeFromDataSource() {
		Cloud cloud = ((MapReduceEngine) CloudSim.getEntity("MapReduceEngine")).getCloud();
		Requests requests = ((MapReduceEngine) CloudSim.getEntity("MapReduceEngine")).getRequests();
		VmInstance vmInstance = requests.getVMInstanceFromId(getVmId());
		VmType vmType = cloud.getVMTypeFromId(vmInstance.VmTypeId);
		return predictFileTransferTimeFromDataSource(vmType, selectedDataSourceName);
	}
	
	public double predictFileTransferTimeFromDataSource(VmType currentVmType, String dataSourceName) {
		Cloud cloud = ((MapReduceEngine) CloudSim.getEntity("MapReduceEngine")).getCloud();

		String currentVmTypeName = currentVmType.name;
		
		for (List<Object> throughputs_vm_ds : cloud.throughputs_vm_ds) {
			String source_vm = (String) throughputs_vm_ds.get(0);
			String destination_dataSource = (String) throughputs_vm_ds.get(1);
			double throughputInMegaBit = (double) throughputs_vm_ds.get(2);

			if (source_vm.equals(currentVmTypeName) && destination_dataSource.equals(dataSourceName))
				return getCloudletFileSize() / (throughputInMegaBit / 8.0);
		}

		try {
			throw new Exception("Throughputs between "+currentVmTypeName+" and "+dataSourceName+" could not be found in Cloud.yaml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0;
	}

	public double predictFileTransferTimeToReduceVms() {
		Cloud cloud = ((MapReduceEngine) CloudSim.getEntity("MapReduceEngine")).getCloud();
		Requests requests = ((MapReduceEngine) CloudSim.getEntity("MapReduceEngine")).getRequests();
		VmInstance vmInstance = requests.getVMInstanceFromId(getVmId());
		VmType vmType = cloud.getVMTypeFromId(vmInstance.VmTypeId);
		return predictFileTransferTimeToReduceVms(vmType);
	}
	
	public double predictFileTransferTimeToReduceVms(VmType currentVmType) {
		Cloud cloud = ((MapReduceEngine) CloudSim.getEntity("MapReduceEngine")).getCloud();
		Requests requests = ((MapReduceEngine) CloudSim.getEntity("MapReduceEngine")).getRequests();
		String currentVmTypeName = currentVmType.name;

		double transferTime = 0.0;

		// Find current request
		Request currentRequest = requests.getRequestFromId(requestId);

		// For each reduce get the transfer Time
		for (ReduceTask reduceTask : currentRequest.job.reduceTasks) {
			if (intermediateData.containsKey(reduceTask.name)) {
				int intermediateDataSizeInMegaByte = intermediateData.get(reduceTask.name);
				
				VmInstance vmInstance = requests.getVMInstanceFromId(reduceTask.getVmId());
				VmType reduceVmType = cloud.getVMTypeFromId(vmInstance.VmTypeId);
				
				String reduceVmTypeName = reduceVmType.name;
				
				//if the Reduce vm is in the same Map vm
				if(currentVmTypeName.equals(reduceVmTypeName))
					break;
				
				boolean foundThroughputs_vm_vm = false;
				for (List<Object> throughputs_vm_vm : cloud.throughputs_vm_vm) {
					String source_vm = (String) throughputs_vm_vm.get(0);
					String destination_vm = (String) throughputs_vm_vm.get(1);
					double throughputInMegaBit = (double) throughputs_vm_vm.get(2);

					if (source_vm.equals(currentVmTypeName) && destination_vm.equals(reduceVmTypeName))
					{
						transferTime += intermediateDataSizeInMegaByte / (throughputInMegaBit / 8.0);
						foundThroughputs_vm_vm = true;
						break;
					}
				}
				if(!foundThroughputs_vm_vm)
					try {
						throw new Exception("Throughputs between "+currentVmTypeName+" and "+reduceVmTypeName+" could not be found in Cloud.yaml");
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}

		return transferTime;
	}
}
