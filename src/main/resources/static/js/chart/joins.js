function getJoinedAt(event) {
    return event.joinedAt;
}

$.getJSON(apiUrl + "/groups/" + groupId + "/join-events", (data) => {
    const dataArray = getDatasetByTemporalEvents(data, getJoinedAt);
    const ctx = document.getElementById("joinsChart");
    makeLineChart(dataArray, ctx, "Joins");
});
