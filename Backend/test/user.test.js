jest.setTimeout(15000); // Set a higher timeout value (e.g., 15 seconds)

/*
const request = require('supertest');
const user = require('server.js');
*/

/*
const path = require('path');
const request = require('supertest');
const app = require(path.resolve(__dirname, '../server'));
*/

const { MongoClient } = require('mongodb');
const request = require('supertest');
const app = require('../server'); // Replace with the actual path to your Express app
//const user = require('../path/to/your/user');

const newUser = {
    "email": "newuserlol@example.com",
    "name": "John Doe",
    "address": "123 Main St",
    "friends": [
      "friend1@example.com",
      "friend2@example.com"
    ],
    "friendRequests": [
      "friend3@example.com"
    ]
  };
/*
  beforeAll(async () => {
    // Set up MongoDB connection before tests
    try {
      const uri = 'mongodb://0.0.0.0:27017'; // Replace with your MongoDB connection string
      client = new MongoClient(uri);
      await client.connect();
      console.log("connected");
    } catch (error) {
      console.error('MongoDB Connection Error:', error);
    }
  });
  
  afterAll(async () => {
    // Close MongoDB connection after all tests
    if (client) {
      await client.close();
    }
  });
  
  beforeEach(() => {
    // Log messages or perform setup before each test if needed
    // Avoid logging directly in beforeAll for async operations
  });
  */

describe('User API Tests', () => {
  // Test createNewUser function
  test('POST /api/userlist should create a new user', async () => {
    const res = await request(app)
      .post('/api/userlist')
      .send(newUser);

    expect(res.status).toBe(201);
    expect(res.body.message).toBe('User created successfully');
  });

  
  test('POST /api/userlist should return an error if the user already exists', async () => {
    const res = await request(app)
      .post('/api/userlist')
      .send(newUser);

    expect(res.status).toBe(409);
    expect(res.body.message).toBe('User with this email already exists');
  });
  /*
  test('POST /api/userlist should return an error if the user already exists', async () => {
    let res;
    try {
      res = await request(app)
        .post('/api/userlist')
        .send(newUser2);
    } catch (error) {
      // Retry the request after a delay
      await new Promise(resolve => setTimeout(resolve, 10000)); // Adjust the delay as needed
      res = await request(app)
        .post('/api/userlist')
        .send(newUser2);
    }
  
    expect(res.status).toBe(201);
    expect(res.body.message).toBe('User with this email already exists');
  });
  */
  

  

  // Add more test cases for other functions...
});




