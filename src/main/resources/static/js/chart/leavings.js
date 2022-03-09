function getLeavingAt(event) {
    return event.leavingAt;
}

$.getJSON(apiUrl + "/groups/" + groupId + "/leave-events", (data) => {
    const dataArray = getDatasetByTemporalEvents(data, getLeavingAt);
    let ctx = document.getElementById("leavingsChart");
    makeLineChart(dataArray, ctx, "Leavings");
});
