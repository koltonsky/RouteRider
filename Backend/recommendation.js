// time gap recommendation finder class
// input: two calendar events
// output: list of points of interest

const {
  Client,
  Status,
  LatLng,
} = require('@googlemaps/google-maps-services-js');

let suggestions = [];
const API_KEY = 'AIzaSyADWClq31r1vS21EWOcBnpOayxFOIDd-YQ';

function callback(results, status) {
  if (status === Status.OK) {
    for (let i = 0; i < results.length; i++) {
      suggestions.push(results[i]);
    }
  }
}

const recommendation = async (event1, event2) => {
  const timeGap = event2.start - event1.end;
  const centerLat = (event1.lat + event2.lat) / 2;
  const centerLng = (event1.lng + event2.lng) / 2;

  const client = new Client();

  const location = [centerLat, centerLng];

  const request = {
    params: {
      key: API_KEY,
      location: location,
      radius: 500,
      type: ['restaurant', 'cafe', 'library'], // TODO: edit type in user preferences
    },
  };

  await client
    .placesNearby(request)
    .then((response) => {
      if (response.data.status === Status.OK) {
        callback(response.data.results, Status.OK);
      } else {
        console.error('Error: ', response.data.error_message);
      }
    })
    .catch((error) => {
      console.error('Network error: ', error);
    });

  return suggestions;
};

module.exports = recommendation;
