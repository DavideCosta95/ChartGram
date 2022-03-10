$.getJSON(apiUrl + "/groups/" + groupId + "/messages", (data) => {
    let dataset = [
        {
            value: data.filter(d => d.type === 1).length,
            label: 'Text'
        },
        {
            value: data.filter(d => d.type === 2).length,
            label: 'Audio'
        },
        {
            value: data.filter(d => d.type === 3).length,
            label: 'Photo'
        },
        {
            value: data.filter(d => d.type === 4).length,
            label: 'Sticker'
        },
        {
            value: data.filter(d => d.type === 5).length,
            label: 'Video'
        },
        {
            value: data.filter(d => d.type === 6).length,
            label: 'Gif'
        },
        {
            value: data.filter(d => d.type === 7).length,
            label: 'Other'
        }
    ];

    let ctx = document.getElementById("messagesTypeChart");
    let pieChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: dataset.map((currentValue) => currentValue.label),
            datasets: [{
                data: dataset.map((currentValue) => currentValue.value),
                backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#feae65', '#d39df6', '#79fe7f', '#fd6970'],
                hoverBackgroundColor: ['#2e59d9', '#17a673', '#2c9faf', '#fc9c45', '#ba61f5', '#3ed445', '#fc4c54'],
                hoverBorderColor: "rgba(234, 236, 244, 1)",
            }],
        },
        options: {
            maintainAspectRatio: false,
            tooltips: {
                backgroundColor: "rgb(255,255,255)",
                bodyFontColor: "#858796",
                borderColor: '#dddfeb',
                borderWidth: 1,
                xPadding: 15,
                yPadding: 15,
                displayColors: false,
                caretPadding: 10,
            },
            legend: {
                display: true
            },
            cutoutPercentage: 0,
        },
    });
});