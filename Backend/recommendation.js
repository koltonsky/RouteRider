// time gap recommendation finder class
// input: two calendar events
// output: list of points of interest

const {
  Client,
  Status,
  //LatLng,
} = require('@googlemaps/google-maps-services-js');

let suggestions = [];
const API_KEY = 'AIzaSyADWClq31r1vS21EWOcBnpOayxFOIDd-YQ';

const recommendation = async (addr1, addr2) => {
  suggestions = [];
  return new Promise(async (resolve, reject) => {  
    // const timeGap = event2.start - event1.end;
    const client = new Client();

    const geocodeRequest1 = {
      params: {
        key: API_KEY,
        address: addr1,
      },
    };
    const geocodeRequest2 = {
      params: {
        key: API_KEY,
        address: addr2,
      },
    };

    let coords1 = { lat: 0, lng: 0 };
    let coords2 = { lat: 0, lng: 0 };

    try {
      const [response1, response2] = await Promise.all([
        client.geocode(geocodeRequest1),
        client.geocode(geocodeRequest2),
      ]);
      /*
      console.log('response1');
      console.log(response1);
      console.log('response2');
      console.log(response2);
      */

      if (response1.data.status === Status.OK) {
        coords1 = response1.data.results[0].geometry.location;
      } else {
        //console.error('Error for addr1: ', response1.data.error_message);
      }

      if (response2.data.status === Status.OK) {
        coords2 = response2.data.results[0].geometry.location;
      } else {
        //console.error('Error for addr2: ', response2.data.error_message);
      }
      const centerLat = (coords1.lat + coords2.lat) / 2;
      const centerLng = (coords1.lng + coords2.lng) / 2;

      const location = [centerLat, centerLng];

      const placesRequest = {
        params: {
          key: API_KEY,
          location,
          radius: 500,
          type: ['restaurant', 'cafe', 'library'],
        },
      };

      const response = await client.placesNearby(placesRequest);
      if (response.data.status === Status.OK) {
        const results = response.data.results;
        for (let i = 0; i < results.length; i++) {
          suggestions.push(results[i]);
        }
        resolve(suggestions);
      } else {
        //console.error('Error: ', response.data.error_message);
        reject(response.data.error_message);
      }
    } catch (error) {
      //console.error('Network error: ', error);
      reject(error);
    }
  });
};

module.exports = recommendation;
