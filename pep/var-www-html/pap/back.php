<?php 



function loadPolicy($conn){
	if(isset($_POST['data'])){
	   if ($conn->connect_error) {trigger_error('Database connection failed: ' . $conn->connect_error, E_USER_ERROR);exit();}
		$query = "SELECT policy FROM policies WHERE name=?";
		$stmt = $conn->prepare($query);
		if ($stmt) {
			$stmt->bind_param("s", $_POST['data']);
			$stmt->execute();
			$stmt->bind_result($policy_text);
			 while ($stmt->fetch()) {
					echo $policy_text;
			}
			$stmt->close();
		} else {
			echo "Prepare failed: (" . $conn->errno . ") " . $conn->error;
			echo "stmt failed";
		}
	}
}
function savePolicy($conn){
	
	if(isset($_POST['name']) && isset($_POST['data'])){
		$pname = $_POST['name'];
		$data = $_POST['data'];
		$query = "UPDATE policies SET policy=? WHERE name=?";
		$stmt = $conn->prepare($query);
		if ($stmt){
			$stmt->bind_param("ss", $data, $pname);
			$stmt->execute();
			$stmt->close();
			echo 'success';
		}
	}else{
		 echo "Prepare failed: (" . $conn->errno . ") " . $conn->error;
		echo 'error';
	}
}
function getPolicyNames($conn){
	   if ($conn->connect_error) {trigger_error('Database connection failed: ' . $conn->connect_error, E_USER_ERROR);exit();}
		$query = "SELECT name FROM policies";
		$stmt = $conn->prepare($query);
		if ($stmt) {
			$stmt->execute();
			$result = $stmt->get_result();
			 while ($data = $result->fetch_assoc())
				{
					$pnames[] = $data;
				}
			$stmt->close();
			
			echo json_encode($pnames);
		} else {
			echo "Prepare failed: (" . $conn->errno . ") " . $conn->error;
			echo "error";
		}
}
function getSelectedPolicyNames($conn){
	   if ($conn->connect_error) {trigger_error('Database connection failed: ' . $conn->connect_error, E_USER_ERROR);exit();}
		$query = "SELECT name FROM selected_policies";
		$stmt = $conn->prepare($query);
		if ($stmt) {
			$stmt->execute();
			$result = $stmt->get_result();
			 while ($data = $result->fetch_assoc())
				{
					$pnames[] = $data;
				}
			$stmt->close();
			
			echo json_encode($pnames);
		} else {
			 echo "Prepare failed: (" . $conn->errno . ") " . $conn->error;
			echo "error";
		}
}
function addSelectedPolicy($conn){
	if(isset($_POST['pname'])){
		$pname = $_POST['pname'];
		$query = "INSERT INTO selected_policies VALUE (?)";
		$stmt = $conn->prepare($query);
		if ($stmt){
			$stmt->bind_param("s", $pname);
			$stmt->execute();
			$stmt->close();
			echo 'success';
		}
	} else{
		 echo "Prepare failed: (" . $conn->errno . ") " . $conn->error;
		echo "error";
	}
}
function removeSelectedPolicy($conn){
	if(isset($_POST['pname'])){
		$pname = $_POST['pname'];
		$query = "DELETE FROM selected_policies WHERE name=?";
		$stmt = $conn->prepare($query);
		if ($stmt){
			$stmt->bind_param("s", $pname);
			$stmt->execute();
			$stmt->close();
			echo 'success';
		}
	}else{
		echo 'error';
	}
}
function addPolicy($conn){
	if(isset($_POST['pname']) && isset($_POST['policy'])){
		$pname = $_POST['pname'];
		$policy = $_POST['policy'];
		$query = "INSERT INTO policies (name, policy) VALUE (?,?)";
		$stmt = $conn->prepare($query);

		if ($stmt){
			$stmt->bind_param("ss", $pname, $policy);
			$stmt->execute();
			$stmt->close();
			echo 'success';
		}else{
			echo "error";
		}
	} else{
		echo "error";
	}
}
function deletePolicy($conn){
	if(isset($_POST['pname'])){
		$pname = $_POST['pname'];
		$query = "DELETE FROM policies WHERE name=?";
		$stmt = $conn->prepare($query);
		if ($stmt){
			$stmt->bind_param("s", $pname);
			$stmt->execute();
			$stmt->close();
			echo 'success';
		}
	}else{
		echo 'error';
	}
}
session_start(); if($_SESSION["https://".$_SERVER["HTTP_HOST"]."/pap/"] == "permit"){
	include("includes/includes.php");
	if(isset($_POST['step'])){
			$step = $_POST['step'];
			if($step == "load"){
				loadPolicy($conn);
			}
			
			if($step == "save"){
				savePolicy($conn);
			}
			
			if($step == "pnames"){
				getPolicyNames($conn);
			}
			
			if($step == "selected"){
				getSelectedPolicyNames($conn);
			}
			
			if($step == "seladd"){
				addSelectedPolicy($conn);
			}
			
			if($step == "selremove"){
				removeSelectedPolicy($conn);
			}
			
			if($step == "addpol"){
				addPolicy($conn);
			}
			
			if($step == "deletepol"){
				deletePolicy($conn);
			}
			
	}else{
			echo "fail";
	}
} else {
	echo "unauthorized";
} 
?>
