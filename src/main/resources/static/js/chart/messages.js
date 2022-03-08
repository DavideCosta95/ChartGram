Chart.defaults.global.defaultFontFamily = 'Nunito', '-apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
Chart.defaults.global.defaultFontColor = '#858796';
var DateTime = luxon.DateTime;
var Duration = luxon.Duration;

function formatDateFragment(n) {
    return n > 10 ? "" + n : "0" + n;
}

$.getJSON(apiUrl + "/groups/" + groupId + "/messages", (data) => {
    let earliestEventTime = DateTime.fromISO(data[0].sentAt);
    let threshold = earliestEventTime
        .plus(Duration.fromMillis(1000*60*60*24))
        .minus(Duration.fromMillis(1000*60*60* earliestEventTime.hour))
        .minus(Duration.fromMillis(1000*60* earliestEventTime.minute))
        .minus(Duration.fromMillis(1000* earliestEventTime.second));

    let dataset = [];
    for (let message of data) {
        let currentEventTime = DateTime.fromISO(message.sentAt);
        if (currentEventTime >= threshold) {
            threshold = threshold.plus(Duration.fromMillis(1000*60*60*24))
        }
        if (dataset[threshold]) {
            dataset[threshold].value += 1;
        } else {
            dataset[threshold] = {
                label: threshold.minus(Duration.fromMillis(1000*60*60*24)),
                value: 1
            };
        }
    }

    let dataArray = [];

    for (let key in dataset) {
        dataArray.push({
            label: dataset[key].label,
            value: dataset[key].value
        });
    }

    let ctx = document.getElementById("messagesChart");
    var lineChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dataArray.map((e) => formatDateFragment(e.label.day) + "/" + formatDateFragment(e.label.month) + "/" + e.label.year),
            datasets: [{
                label: "Messages",
                lineTension: 0.3,
                backgroundColor: "rgba(78, 115, 223, 0.05)",
                borderColor: "rgba(78, 115, 223, 1)",
                pointRadius: 3,
                pointBackgroundColor: "rgba(78, 115, 223, 1)",
                pointBorderColor: "rgba(78, 115, 223, 1)",
                pointHoverRadius: 3,
                pointHoverBackgroundColor: "rgba(78, 115, 223, 1)",
                pointHoverBorderColor: "rgba(78, 115, 223, 1)",
                pointHitRadius: 10,
                pointBorderWidth: 2,
                data: dataArray.map((e) => e.value),
            }],
        },
        options: {
            maintainAspectRatio: false,
            layout: {
                padding: {
                    left: 10,
                    right: 25,
                    top: 25,
                    bottom: 0
                }
            },
            scales: {
                xAxes: [{
                    time: {
                        unit: 'date'
                    },
                    gridLines: {
                        display: false,
                        drawBorder: false
                    },
                    ticks: {
                        maxTicksLimit: 20
                    }
                }],
                yAxes: [{
                    ticks: {
                        maxTicksLimit: 20,
                        padding: 1
                    },
                    gridLines: {
                        color: "rgb(234, 236, 244)",
                        zeroLineColor: "rgb(234, 236, 244)",
                        drawBorder: false,
                        borderDash: [2],
                        zeroLineBorderDash: [2]
                    }
                }],
            },
            legend: {
                display: false
            },
            tooltips: {
                backgroundColor: "rgb(255,255,255)",
                bodyFontColor: "#858796",
                titleMarginBottom: 10,
                titleFontColor: '#6e707e',
                titleFontSize: 14,
                borderColor: '#dddfeb',
                borderWidth: 1,
                xPadding: 15,
                yPadding: 15,
                displayColors: false,
                intersect: false,
                mode: 'index',
                caretPadding: 10
            }
        }
    });
});
