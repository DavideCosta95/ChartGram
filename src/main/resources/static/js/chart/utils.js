const DateTime = luxon.DateTime;
const Duration = luxon.Duration;
const millisInDay = 1000 * 60 * 60 * 24;

function formatDateFragment(n) {
    return n > 10 ? "" + n : "0" + n;
}

function defaultEmptyDataset() {
    const dataset = [];
    const daysNumber = 10;
    const startingDate = DateTime.now().minus(Duration.fromMillis(millisInDay * (daysNumber - 1)));
    for (let day = 0; day < daysNumber; day++) {
        dataset.push(
            {
                label: startingDate.plus(Duration.fromMillis(millisInDay * day)),
                value: 0
            });
    }
    return dataset;
}

function formatDate(date) {
    return formatDateFragment(date.day) + "/" + formatDateFragment(date.month) + "/" + ("" + date.year).substring(2, 4);
}

function getDatasetByTemporalEvents(data, getTimestampFunction) {
    let dataset = [];
    if (data.length > 0) {
        const earliestEventTime = DateTime.fromISO(getTimestampFunction(data[0]));
        let threshold = earliestEventTime
            .minus(Duration.fromMillis(1000 * 60 * 60 * earliestEventTime.hour))
            .minus(Duration.fromMillis(1000 * 60 * earliestEventTime.minute))
            .minus(Duration.fromMillis(1000 * earliestEventTime.second));

        for (let event of data) {
            let currentEventTime = DateTime.fromISO(getTimestampFunction(event));
            while (currentEventTime >= threshold) {
                threshold = threshold.plus(Duration.fromMillis(millisInDay));
                dataset[threshold] = {
                    label: threshold.minus(Duration.fromMillis(millisInDay)),
                    value: 0
                };
            }
            dataset[threshold].value += 1;
        }
    }

    let dataArray = [];
    for (let key in dataset) {
        dataArray.push({
            label: dataset[key].label,
            value: dataset[key].value
        });
    }

    if (dataArray.length === 0) {
        dataArray = defaultEmptyDataset();
    }
    return dataArray;
}

function makeLineChart(dataArray, context, label) {
    new Chart(context, {
        type: 'line',
        data: {
            labels: dataArray.map((e) => formatDate(e.label)),
            datasets: [{
                label: label,
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
}