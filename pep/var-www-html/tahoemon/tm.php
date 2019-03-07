<html>

<head>
    <meta charset="utf-8" />
    <title>Tahoe Grid Monitor</title>
    <style>
        table {border-collapse: collapse;}
        table, th, td {border: 1px solid black;}
        td {padding: 10px;}
    </style>
    <script src="jquery-3.3.1.min.js" type="text/ecmascript"></script>
    <script src="canvasjs.min.js"></script>
</head>

<body>
    <div id="main" style="width:50%;margin:0 auto;">
        <h3 style="width:50%; margin:0 auto;text-align:center;">Tahoe Grid Health</h3>
        <h5 style="width:50%; margin:0 auto;text-align:center;" id="fadeout">Updated...</h5>
        <br>
        <table style="width:50%; margin:0 auto;" id="tahoemon">
            <tr id="tbl-head">
            </tr>
            <tr id="tbl-body">
            </tr>
        </table>
    </div>
        <br>
    <div id="chartContainer" style="height: 70%; width:100%;"></div>

    <script type="text/ecmascript">
        function getRandomNum(min, max) {
         return Math.floor(Math.random()*(max-min+1)+min);
        }
	var type = "<?php
			if(isset($_GET["c"]) && !empty($_GET["c"])){
				echo $_GET["c"];
			}else{
				echo "line";
			}
		?>";

        //  config & setup
        var grid = {
            ctidev1: "https://ctidev4.critical.com/tahoemon/d.php?n=1",
            ctidev2: "https://ctidev4.critical.com/tahoemon/d.php?n=2",
            ctidev3: "https://ctidev4.critical.com/tahoemon/d.php?n=3",
            ctidev4: "https://ctidev4.critical.com/tahoemon/d.php?n=4",
            ctidev5: "https://ctidev4.critical.com/tahoemon/d.php?n=5"
        }

        dps = {};
        for (var key in grid) {
            dps[key] = [
                {x: 0,y: 5},
                {x: 1,y: 4},
                {x: 2,y: 10},
                {x: 3,y: 4},
                {x: 4,y: 3},
                {x: 5,y: 7},
                {x: 6,y: 0},
                {x: 7,y: 2},
                {x: 8,y: 0},
                {x: 9,y: 9},
            ];
        }

        dpc = {};
        for (var key in grid) {
            dpc[key] = [0, 0, 0];
        }

        var counter = 10;
        var table = document.getElementById("tahoemon");
        console.log("Initializing with: ");

        for (var key in grid) {
            console.log("Server named: " + key + " located at " + grid[key]);
            var th = document.createElement("th");
            th.appendChild(document.createTextNode(key));
            th.id = key + "-header";

            var td = document.createElement("td");
            td.appendChild(document.createTextNode("pending"));
            td.id = key + "-body";
            td.style = "color:grey";

            var trth = document.getElementById("tbl-head");
            trth.appendChild(th);

            var trtd = document.getElementById("tbl-body");
            trtd.appendChild(td);
        }

        function check_health() {
            for (var key in grid) {
                $.ajax({
                    url: grid[key],
                    type: 'GET',
                    //async: false,
                    key: key,
                    datatype: "json",
                    timeout: 1000, // 1000 = 1 second // seems to take ~250ms?
                    success: function (data) {
                        try{
                                var json = $.parseJSON(data);
                                if(!$.isEmptyObject(json.counters) && "storage_server.read" in json.counters){
                                       	dpc[this.key].push(json.counters["storage_server.read"]);
                                        //dpc[this.key].push(json.counters["storage_server.read"] + getRandomNum(500,2000));

                                        dpc[this.key].shift();
                                        var node_health = document.getElementById(this.key + "-body");
                                        node_health.innerHTML = "alive";
                                        node_health.style = "color:green";
                                }else{
                                        //dpc[this.key].push(0 + getRandomNum(500,2000));
                                        dpc[this.key].push(0);
                                        dpc[this.key].shift();
                                        var node_health = document.getElementById(this.key + "-body");
                                        node_health.innerHTML = "alive";
                                        node_health.style = "color:green";
                                }
                        }catch(err){
                                dpc[this.key].push(0);
                                dpc[this.key].shift();
                                var node_health = document.getElementById(this.key + "-body");
                                node_health.innerHTML = "dead";
                                node_health.style = "color:red";
                                console.log(err);
                        }
                    },
                    error: function (xhr, error) {
                        dpc[this.key].push(0);
                        dpc[this.key].shift();
                        var node_health = document.getElementById(this.key + "-body");
                        node_health.innerHTML = "dead";
                        node_health.style = "color:red";
                        if (xhr.statusText == "timeout") {
                            console.log("ajax timed out");
                        } else {
                            // display error message
                            console.log("Error response:" + xhr.response);
                            console.log("Error status:" + xhr.status);
                            console.log("Error statusText:" + xhr.statusText);
                        }
                    }
                });
            }
        }


	<?php
		if(isset($_GET["t"]) && !empty($_GET["t"])){
			$interval = $_GET["t"];
		}else{
			$interval = 2000;
		}

		if(isset($_GET["dl"]) && !empty($_GET["dl"])){
			$datalength = $_GET["dl"];
		}else{
			$datalength = 20;
		}

	?>

        /////////////////// begin chart stuff
        var datalength = <?php echo $datalength;?>;

        var d = [];
        for (var key2 in grid) {
            d.push({
                type: type,
                yValueFormatString: "Read #########",
                xValueFormatString: "Time #####",
                showInLegend: true,
                name: key2,
                dataPoints: dps[key2]
            });
        }

        var chart = new CanvasJS.Chart("chartContainer", {
            zoomEnabled: true,
            title: {
                text: "Tahoe Grid Read Counts"
            },
            axisX: {
                title: "chart updates every <?php echo $interval/1000; ?> secs"
            },
            axisY: {
                includeZero: true,
		maximum: 50
            },
            toolTip: {
                shared: true
            },
            legend: {
                cursor: "pointer",
                verticalAlign: "top",
                fontSize: 22,
                fontColor: "dimGrey",
                itemclick: toggleDataSeries
            },
            data: d
        });

        function toggleDataSeries(e) {
            if (typeof (e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
                e.dataSeries.visible = false;
            } else {
                e.dataSeries.visible = true;
            }
            chart.render();
        }

        function updateChart() {
            counter += 1;
            var idx = 0;
            for (var key1 in grid) {
                var yval = Math.abs(
                                dpc[key1][dpc[key1].length-1] - dpc[key1][dpc[key1].length-2]
                        );
		// this makes overlapping lines easier to see
		if(yval != 0){
			yval = yval + getRandomNum(0,4);
		}

                dps[key1].push({
                    x: counter,
                    y: yval
                });
                if (dps[key1].length > datalength) {
                    dps[key1].shift();
                }
                chart.options.data[idx].legendText = " " + key1 + " " + yval;
                idx += 1;

            }
                chart.render();
        }
        //////////// end chart stuff

        setInterval(function () {
            check_health();
            updateChart();
            document.getElementById("fadeout").style.opacity = 1;
            $('#fadeout').fadeTo('slow', 0, function () {});
        }, <?php echo $interval; ?>);

    </script>
</body>

</html>
