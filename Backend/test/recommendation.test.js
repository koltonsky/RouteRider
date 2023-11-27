//const recommendation = require('../recommendation');
const supertest = require('supertest');
const { app } = require('../server'); // Replace with the actual path to your Express app
const request = supertest(app);
// Mocking the Google Maps API responses
/*
jest.mock('@googlemaps/google-maps-services-js', () => ({
  Client: jest.fn(() => ({
    geocode: jest.fn(),
    placesNearby: jest.fn(),
  })),
  Status: {
    OK: 'OK',
  },
}));
*/

/*
describe('recommendation function', () => {
  test('should return a list of suggestions for valid addresses', async () => {
    const addr1 = 'UBC MacMillan, Room 360';
    const addr2 = 'UBC MacLeod, Room 4006';
    const suggestions = await recommendation(addr1, addr2);
    expect(suggestions).toHaveLength(6); // Assuming three types of places are requested (restaurant, cafe, library)
  });

  test('should handle errors for invalid addresses', async () => {
    const invalidAddr1 = 'Invalid Address 1';
    const invalidAddr2 = 'Invalid Address 2';

    await expect(recommendation(invalidAddr1, invalidAddr2)).rejects.toThrow();
  });

  test('should handle network errors', async () => {
    // Mocking a network error by rejecting the geocode promise
    jest.spyOn(Promise, 'all').mockRejectedValueOnce('Network error');

    const addr1 = '123 Main St, City1, Country1';
    const addr2 = '456 Second St, City2, Country2';

    await expect(recommendation(addr1, addr2)).rejects.toThrow('Network error');
  });

  test('should handle Google Maps API errors', async () => {
    // Mocking an API error by setting response status to NOT_FOUND
    jest.spyOn(Promise, 'all').mockResolvedValueOnce([
      { data: { status: 'OK', results: [{ geometry: { location: { lat: 1, lng: 1 } } }] } },
      { data: { status: 'NOT_FOUND', error_message: 'Address not found' } },
    ]);

    const addr1 = '123 Main St, City1, Country1';
    const addr2 = 'Invalid Address 2';

    await expect(recommendation(addr1, addr2)).rejects.toThrow('Address not found');
  });

  // Add more test cases for edge cases, different input combinations, etc.
});
*/

// Interface GET https://20.163.28.92:8081/api/recommendation/timegap/:addr1/:addr2
describe('GET /api/recommendation/timegap/:addr1/:addr2', () => {

    // Input: addr1, addr2
// Expected status code: 200
// Expected behavior: Retrieve 6 nearby events
// Expected output: 6 nearby events
// ChatGPT usage: Yes
    test('should return a list of suggestions for valid addresses', async () => {
        const addr1 = 'UBC MacMillan, Room 360';
        const addr2 = 'UBC MacLeod, Room 4006';
        //await request.get(`/api/userlist/test`);
      const response = await request.get(`/api/recommendation/timegap/${addr1}/${addr2}`);
      
      expect(response.status).toBe(200);
      expect(response.body.suggestions).toHaveLength(7);
    });

    
        // Input: addr1, addr2 (both invalid)
// Expected status code: 500
// Expected behavior: No nearby events displayed
// Expected output: error{"An error occured"}
// ChatGPT usage: Yes
  test('should handle errors for invalid addresses', async () => {
    const response = await request.get('/api/recommendation/timegap/Invalid%20Address%201/Invalid%20Address%202');

    expect(response.status).toBe(500);
    expect(response.body.error).toBe('An error occurred');
  });

          // Input: addr1, addr2 (network error invoked)
// Expected status code: 500
// Expected behavior: No nearby events displayed
// Expected output: error{"An error occured"}
// ChatGPT usage: Yes
  test('should handle network errors', async () => {
    // Mocking a network error by rejecting the geocode promise
    jest.spyOn(Promise, 'all').mockRejectedValueOnce('Network error');

    const response = await request.get('/api/recommendation/timegap/123%20Main%20St,%20City1,%20Country1/456%20Second%20St,%20City2,%20Country2');

    expect(response.status).toBe(500);
    expect(response.body.error).toBe('An error occurred');
  });

  /*
    // Input: addr1, addr2 (Google Maps API error invoked)
// Expected status code: 500
// Expected behavior: No nearby events displayed
// Expected output: error{"Address not found"}
// ChatGPT usage: Yes
  test('should handle Google Maps API errors', async () => {
    // Mocking an API error by setting response status to NOT_FOUND
    jest.spyOn(Promise, 'all').mockResolvedValueOnce([
      { data: { status: 'OK', results: [{ geometry: { location: { lat: 1, lng: 1 } } }] } },
      { data: { status: 'NOT_FOUND', error_message: 'Address not found' } },
    ]);

    const response = await request.get('/api/recommendation/timegap/123%20Main%20St,%20City1,%20Country1/Invalid%20Address%202');

    expect(response.status).toBe(500);
    expect(response.body.error).toBe('Address not found');
  });
  */
});
