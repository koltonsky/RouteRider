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
/*
const request = require('supertest');
const app = require('../server'); // Replace with the actual path to your Express app
//const user = require('../path/to/your/user');
*/
const supertest = require('supertest');
const { app, stopSSLServer} = require('../server'); // Replace with the actual path to your Express app
const request = supertest(app);

const user = {
    "email": "newuserlol3@example.com",
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

//const userEmail = 'koltonluu@gmail.com';
//const nonExistingEmail = 'nonexistinguser@example.com';
//let server;


  beforeAll(async () => {
    //server = app.listen(8081);
    // Set up MongoDB connection before tests
    try {
      const uri = 'mongodb://127.0.0.1:27017'; // Replace with your MongoDB connection string
      client = new MongoClient(uri);
      await client.connect();
      console.log("connected");
    } catch (error) {
      console.error('MongoDB Connection Error:', error);
    }
  });
  
  
  afterAll(async () => {
    
    if (client) {
      await client.close();
    }
    //closeServer();
    stopSSLServer();    
  });
  
  
  beforeEach(() => {
    // Log messages or perform setup before each test if needed
    // Avoid logging directly in beforeAll for async operations
  });
  

// Interface POST https://20.163.28.92:8081/api/userlist
describe('Create a new user', () => {
  // Mock user data for testing
  let userData; // Declare scheduleData outside to use in different test cases
  
  // Set up the test data before running the tests
  beforeAll(async () => {
    userData = {
      email: 'userTHING@example.com',
      // Add other schedule data properties as needed
    };
  });

  // Clean up the test data after running all the tests
  afterAll(async () => {
    // Assuming you have already connected to the MongoDB client
    const collection = client.db('UserDB').collection('userlist');
    //await collection.deleteOne({ email: userData.email });
    //await collection.deleteOne({ email: userData.email });
    await collection.deleteOne({ email: userData.email });
  });

// Input: nonexisting user
// Expected status code: 201
// Expected behavior: create a new user
// Expected output: message: User created 
// ChatGPT usage: Yes
  test('POST /api/userlist should create a new user', async () => {
    const res = await request
      .post('/api/userlist')
      .send(userData);

    expect(res.status).toBe(201);
    expect(res.body.message).toBe('User created successfully');
  });

// Input: existing user
// Expected status code: 409
// Expected behavior: return an error if the user already exists
// Expected output: message: User with this email already exists
// ChatGPT usage: Yes
  test('POST /api/userlist should return an error if the user already exists', async () => {
    const res = await request
      .post('/api/userlist')
      .send(userData);

    expect(res.status).toBe(409);
    expect(res.body.message).toBe('User with this email already exists');
  });


});


// Interface GET https://20.163.28.92:8081/api/userlist/:email
describe('Get an email', () => {
  // Define variables for storing test data
  const userEmail = 'testg@example.com';
  const nonExistingEmail = 'nonexistent@example.com';

  // Before running the tests, add a sample user to the database
  beforeAll(async () => {
    // Assuming client and request are properly initialized
    const collection = client.db('UserDB').collection('userlist');
    await collection.insertOne({ email: userEmail });
  });

  // After running the tests, remove the sample user from the database
  afterAll(async () => {
    // Assuming client is properly initialized
    const collection = client.db('UserDB').collection('userlist');
    await collection.deleteOne({ email: userEmail });
  });

  // Input: existing user email
  // Expected status code: 200
  // Expected behavior: retrieve an existing user by email
  // Expected output: user object
  // ChatGPT usage: Yes
  test('GET /api/userlist/:email should retrieve an existing user by email', async () => {
    const res = await request.get(`/api/userlist/${userEmail}`);

    expect(res.status).toBe(200);
    expect(res.body.email).toBe(userEmail);
  });

  // Input: non-existing user email
 // Expected status code: 404
 // Expected behavior: return an error if the user does not exist
 // Expected output: error: User not found
 // ChatGPT usage: Yes
  test('GET /api/userlist/:email should return an error for a non-existing user', async () => {
    const res = await request.get(`/api/userlist/${nonExistingEmail}`);

    expect(res.status).toBe(404);
    expect(res.body.error).toBe('User not found');
  });

});

// Interface PUT https://20.163.28.92:8081/api/userlist/:email
describe('Update User Address', () => {
  // Define variables for storing test data
  const userEmail = 'testa@example.com';
  const nonExistingEmail = 'nonexistent@example.com';

  // Before running the tests, add a sample user to the database
  beforeAll(async () => {
    // Assuming client and request are properly initialized
    const collection = client.db('UserDB').collection('userlist');
    await collection.insertOne({ email: userEmail, address: 'Initial Address' });
  });

  // After running the tests, remove the sample user from the database
  afterAll(async () => {
    // Assuming client is properly initialized
    const collection = client.db('UserDB').collection('userlist');
    await collection.deleteOne({ email: userEmail });
  });

  // Input: existing user email, address
  // Expected status code: 200
  // Expected behavior: update an existing user address
  // Expected output: message: User address updated successfully
  // ChatGPT usage: Yes
  test('PUT /api/userlist/:email/address should update user address successfully', async () => {
    const newAddress = '123 Main St';

    const res = await request
      .put(`/api/userlist/${userEmail}/address`)
      .send({ address: newAddress });

    expect(res.status).toBe(200);
    expect(res.body.message).toBe('User address updated successfully');
  });

  // Input: non-existing user email, address
 // Expected status code: 404
 // Expected behavior: return an error if the user does not exist
 // Expected output: error: User not found
 // ChatGPT usage: Yes
  test('PUT /api/userlist/:email/address should return an error for a non-existing user', async () => {
    const newAddress = '456 Oak St';

    const res = await request
      .put(`/api/userlist/${nonExistingEmail}/address`)
      .send({ address: newAddress });

    expect(res.status).toBe(404);
    expect(res.body.error).toBe('User not found');
  });

});

// Interface GET https://20.163.28.92:8081/api/userlist/:email/friends
describe('Get User Friend List with Names', () => {
  // Define variables for storing test data
  const userEmail = 'test1@example.com';
  const nonExistingEmail = 'nonexistent@example.com';
  const userEmailWithNoFriends = 'userwithnofriends@example.com';
  const userEmailWithFriendsNoRequests = 'userwithfriendsnorequests@example.com';
  const userEmailWithRequestsNoFriends = 'userwithrequestsnofriends@example.com';

  // Before running the tests, add a sample user to the database
  beforeAll(async () => {
    // Assuming client and request are properly initialized
    const collection = client.db('UserDB').collection('userlist');

    await collection.insertOne({
      email: userEmail,
      name: 'test1@example.com',
      friends: ['friend1@example.com'],
      friendRequests: ['requester@example.com']
    });

    await collection.insertOne({
      email: 'friend1@example.com',
      name: "friend1",
      friends: [userEmail],
      friendRequests: ['requester@example.com']
    });

    await collection.insertOne({
      email: 'requester@example.com',
      name: "requester",
      friends: [userEmailWithFriendsNoRequests],
      friendRequests: []
    });

    await collection.insertOne({
      email: userEmailWithNoFriends,
      name: 'UserWithNoFriends',
      friends: [],
      friendRequests: []
    });

    await collection.insertOne({
      email: userEmailWithFriendsNoRequests,
      name: 'UserWithNoFriends',
      friends: ["requester@example.com"],
      friendRequests: []
    });

    await collection.insertOne({
      email: userEmailWithRequestsNoFriends,
      name: 'UserWithNoFriends',
      friends: [],
      friendRequests: ["requester@example.com"]
    });
  });

  // After running the tests, remove the sample user from the database
  afterAll(async () => {
    // Assuming client is properly initialized
    const collection = client.db('UserDB').collection('userlist');
    await collection.deleteOne({ email: 'test1@example.com' });
    await collection.deleteOne({ email: 'friend1@example.com' });
    await collection.deleteOne({ email: 'requester@example.com' });
    await collection.deleteOne({ email: userEmailWithNoFriends });
    await collection.deleteOne({ email: userEmailWithFriendsNoRequests });
    await collection.deleteOne({ email: userEmailWithRequestsNoFriends });
  });


  // Input: existing user email
  // Expected status code: 200
  // Expected behavior: retrieve an existing user friend list with names and emails
  // Expected output: friendsWithNames: array of friend objects with name and email properties
  //                  friendRequestsWithNames: array of friend request objects with name and email properties
  // ChatGPT usage: Yes
  test('GET friends', async () => {
    const res = await request
      .get(`/api/userlist/${userEmail}/friends`);

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('friendsWithNames');
    expect(res.body).toHaveProperty('friendRequestsWithNames');
    expect(Array.isArray(res.body.friendsWithNames)).toBe(true);
    expect(Array.isArray(res.body.friendRequestsWithNames)).toBe(true);
  });

// Input:   non-existing user email
// Expected status code: 404
// Expected behavior: return an error if the user does not exist
// Expected output: error: User not found
// ChatGPT usage: Yes
  test('GET /api/userlist/:email/friends should return an error for a non-existing user', async () => {
    const res = await request
      .get(`/api/userlist/${nonExistingEmail}/friends`);

    expect(res.status).toBe(404);
    expect(res.body.error).toBe('User not found');
  });

  // Input: existing user email
// Expected status code: 200
// Expected behavior: return empty list for friends and friendRequests
// Expected output: empty list for friends and friendRequests
// ChatGPT usage: Yes
  test('get friendless', async () => {
    // Assuming client and request are properly initialized
    //const userEmailWithNoFriends = 'userwithnofriends@example.com';
  
    // Add the user to the database with no friends or friend requests

  
    const res = await request
      .get(`/api/userlist/${userEmailWithNoFriends}/friends`);
  
    expect(res.status).toBe(201);
    expect(res.body.friendsWithNames).toEqual([]);
    expect(res.body.friendRequestsWithNames).toEqual([]);
  });

    // Input: existing user email
// Expected status code: 200
// Expected behavior: return list for friends empty list and friendRequests
// Expected output: list for friends empty list and friendRequests
// ChatGPT usage: Yes
  test('GET friends should handle case with friends but no friend requests', async () => {
    const res = await request
      .get(`/api/userlist/${userEmailWithFriendsNoRequests}/friends`);

    expect(res.status).toBe(200);
    expect(Array.isArray(res.body.friendsWithNames)).toBe(true);
    expect(Array.isArray(res.body.friendRequestsWithNames)).toBe(true);
    expect(res.body.friendRequestsWithNames).toEqual([]);
  });

    // Input: existing user email
// Expected status code: 200
// Expected behavior: return empty list for friends and list and friendRequests
// Expected output: empty list for friends list and friendRequests
// ChatGPT usage: Yes
  test('GET friends should handle case with friend requests but no friends', async () => {
    const res = await request
      .get(`/api/userlist/${userEmailWithRequestsNoFriends}/friends`);

    expect(res.status).toBe(200);
    expect(Array.isArray(res.body.friendsWithNames)).toBe(true);
    expect(Array.isArray(res.body.friendRequestsWithNames)).toBe(true);
    expect(res.body.friendsWithNames).toEqual([]);
  });


});

// Interface POST https://20.163.28.92:8081/api/userlist/:email/friendRequest
describe('Send Friend Request', () => {
  // Define variables for storing test data
  const userEmail = 'test2@example.com';
  const nonExistingEmail = 'nonexistent@example.com';
  const existingFriendEmail = 'friend1@example.com';

  // Before running the tests, add a sample user and friend to the database
  beforeAll(async () => {
    // Assuming client and request are properly initialized
    const collection = client.db('UserDB').collection('userlist');

    await collection.insertOne({
      email: 'test2@example.com',
      name: "user2",
      friends: ['friend1@example.com'],
      friendRequests: []
    });

    await collection.insertOne({
      email: 'friend1@example.com',
      name: "friend1",
      friends: [userEmail],
      friendRequests: ['requester@example.com']
    });

    await collection.insertOne({
      email: 'requester@example.com',
      name: "requester",
      friends: [],
      friendRequests: []
    });
  });

  // After running the tests, remove the sample user and friend from the database
  afterAll(async () => {
    // Assuming client is properly initialized
    const collection = client.db('UserDB').collection('userlist');
    await collection.deleteOne({ email: 'test2@example.com' });
    await collection.deleteOne({ email: 'friend1@example.com' });
    await collection.deleteOne({ email: 'requester@example.com' });
  });

  // Input: existing user email, existing friend email
// Expected status code: 200
// Expected behavior: send a friend request successfully
// Expected output: message: Friend request sent successfully
// ChatGPT usage: Yes
  test('POST /api/userlist/:email/friendRequest should send a friend request successfully', async () => {
    const friendEmail = 'requester@example.com';

    const res = await request
      .post(`/api/userlist/${userEmail}/friendRequest`)
      .send({email: friendEmail});

    expect(res.status).toBe(200);
    expect(res.body.message).toBe('Friend request sent successfully');
  });

// Input: non-existing user email, existing friend email
// Expected status code: 404
// Expected behavior: return an error if the user does not exist
// Expected output: error: User not found
// ChatGPT usage: Yes
  test('POST /api/userlist/:email/friendRequest should return an error for a non-existing user', async () => {
    const friendEmail = 'nonexistingfriend@example.com';

    const res = await request
      .post(`/api/userlist/${nonExistingEmail}/friendRequest`)
      .send({email: friendEmail});

    expect(res.status).toBe(221);
    expect(res.body.message).toBe('User not found');
  });

// Input: existing user email, non-existing friend email
// Expected status code: 404
// Expected behavior: return an error if the friend does not exist
// Expected output: error: Friend not found in the userlist
// ChatGPT usage: Yes
  test('POST /api/userlist/:email/friendRequest should return an error for a non-existing friend', async () => {
    const friendEmail = 'nonexistingfriend@example.com';

    const res = await request
      .post(`/api/userlist/${userEmail}/friendRequest`)
      .send({email: friendEmail});

    console.log("MESSAGE: " + res.body.message);

    expect(res.status).toBe(222);
    expect(res.body.message).toBe('Friend not found in the userlist');
    
  });

  // Input: existing user email, existing friend email
// Expected status code: 252
// Expected behavior: return an error if the friend request has already been sent
// Expected output: error: Already friends with this user
// ChatGPT usage: Yes
  test('POST /api/userlist/:email/friendRequest should return an error for an existing friend', async () => {
    const res = await request
      .post(`/api/userlist/${userEmail}/friendRequest`)
      .send({email: existingFriendEmail});

    expect(res.status).toBe(252);
    expect(res.body.message).toBe('Already friends with this user');
  });

  // Input: existing user email, existing friend email
// Expected status code: 251
// Expected behavior: return an error if the friend request has already been sent
// Expected output: You have already received a friend request from this person
// ChatGPT usage: Yes
test('POST /api/userlist/:email/friendRequest should say friend request already received', async () => {
  const res = await request
    .post(`/api/userlist/friend1@example.com/friendRequest`)
    .send({email: "requester@example.com"});

  expect(res.status).toBe(251);
  expect(res.body.message).toBe('You have already received a friend request from this person');
});

  // Input: existing user email, existing friend email
// Expected status code: 250
// Expected behavior: return an error if the friend request has already been sent
// Expected output: error: Friend request already sent
// ChatGPT usage: Yes
test('POST /api/userlist/:email/friendRequest should say friend request already sent', async () => {
  const res = await request
    .post(`/api/userlist/requester@example.com/friendRequest`)
    .send({email: "friend1@example.com"});

  expect(res.status).toBe(250);
  expect(res.body.message).toBe('Friend request already sent');
});

});

// Interface POST https://20.163.28.92:8081/api/userlist/:email/:friendRequest/accept
  describe('Accept Friend Request', () => {
    // Test for successfully accepting a friend request
    const userEmail = 'test3@example.com';
    const nonExistingEmail = 'nonexistent@example.com';
    const friendEmail = 'requester@example.com';
  
    // Before running the tests, add a sample user and friend to the database
    beforeAll(async () => {
      // Assuming client and request are properly initialized
      const collection = client.db('UserDB').collection('userlist');
  
      await collection.insertOne({
        email: 'test3@example.com',
        name: "user3",
        friends: ['friend1@example.com'],
        friendRequests: ['requester@example.com']
      });
  
      await collection.insertOne({
        email: 'friend1@example.com',
        name: "friend1",
        friends: [userEmail],
        friendRequests: ['requester@example.com']
      });
  
      await collection.insertOne({
        email: 'requester@example.com',
        name: "requester",
        friends: [],
        friendRequests: []
      });
    });
  
    // After running the tests, remove the sample user and friend from the database
    afterAll(async () => {
      // Assuming client is properly initialized
      const collection = client.db('UserDB').collection('userlist');
      await collection.deleteOne({ email: 'test3@example.com' });
      await collection.deleteOne({ email: 'friend1@example.com' });
      await collection.deleteOne({ email: 'requester@example.com' });
    });

    // Input: existing user email, existing friend email
// Expected status code: 200
// Expected behavior: accept a friend request successfully
// Expected output: message: Friend request accepted successfully
// ChatGPT usage: Yes
    test('POST /api/userlist/:email/:friendRequest/accept should accept a friend request successfully', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
      //const friendEmail = 'friend@example.com';
  
      // Assuming a friend request has been sent
      /*
      await request
        .post(`/api/userlist/${friendEmail}/friendRequest`)
        .send({ email: userEmail });
        */

      const res = await request
        .post(`/api/userlist/${userEmail}/${friendEmail}/accept`);
  
      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Friend request accepted successfully');
    });
  
// Input: non-existing user email, existing friend email
// Expected status code: 404
// Expected behavior: return an error if the user does not exist
// Expected output: error: User not found
// ChatGPT usage: Yes
    test('POST /api/userlist/:email/:friendRequest/accept should return an error for a non-existing user', async () => {
      // Assuming userEmail is a valid email that exists in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request
        .post(`/api/userlist/${nonExistingEmail}/${friendEmail}/accept`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User not found');
    });
  
// Input: existing user email, non-existing friend email
// Expected status code: 404
// Expected behavior: return an error if the friend does not exist
// Expected output: error: Friend not found in the userlist
// ChatGPT usage: Yes
    test('POST /api/userlist/:email/:friendRequest/accept should return an error for a non-existing friend request', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request
        .post(`/api/userlist/${userEmail}/${friendEmail}/accept`);
  
      expect(res.status).toBe(400);
      expect(res.body.error).toBe("Friend request not found in the user's friend requests");
    });

  });

  // Interface DELETE https://20.163.28.92:8081/api/userlist/:email/:friendRequest/decline
  describe('Decline Friend Request', () => {
    // Test for successfully declining a friend request
    const userEmail = 'test4@example.com';
    const nonExistingEmail = 'nonexistent@example.com';
    const friendEmail = 'requester@example.com';
  
    // Before running the tests, add a sample user and friend to the database
    beforeAll(async () => {
      // Assuming client and request are properly initialized
      const collection = client.db('UserDB').collection('userlist');
  
      await collection.insertOne({
        email: 'test4@example.com',
        name: "user4",
        friends: ['friend1@example.com'],
        friendRequests: ['requester@example.com']
      });
  
      await collection.insertOne({
        email: 'friend1@example.com',
        name: "friend1",
        friends: [userEmail],
        friendRequests: ['requester@example.com']
      });
  
      await collection.insertOne({
        email: 'requester@example.com',
        name: "requester",
        friends: [],
        friendRequests: []
      });
    });
  
    // After running the tests, remove the sample user and friend from the database
    afterAll(async () => {
      // Assuming client is properly initialized
      const collection = client.db('UserDB').collection('userlist');
      await collection.deleteOne({ email: 'test4@example.com' });
      await collection.deleteOne({ email: 'friend1@example.com' });
      await collection.deleteOne({ email: 'requester@example.com' });
    });

    // Input: existing user email, existing friend email
// Expected status code: 200
// Expected behavior: decline a friend request successfully
// Expected output: message: Friend request declined successfully
// ChatGPT usage: Yes
    test('DELETE /api/userlist/:email/:friendRequest/decline should decline a friend request successfully', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
  
      const res = await request
        .delete(`/api/userlist/${userEmail}/${friendEmail}/decline`);
  
      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Friend request declined successfully');
    });
  
// Input: non-existing user email, existing friend email
// Expected status code: 404
// Expected behavior: return an error if the user does not exist
// Expected output: error: User not found
// ChatGPT usage: Yes
    test('DELETE /api/userlist/:email/:friendRequest/decline should return an error for a non-existing user', async () => {
      // Assuming userEmail is a valid email that exists in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request
        .delete(`/api/userlist/${nonExistingEmail}/${friendEmail}/decline`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User not found');
    });
  
// Input: existing user email, non-existing friend email
// Expected status code: 404
// Expected behavior: return an error if the friend does not exist
// Expected output: error: Friend not found in the userlist
// ChatGPT usage: Yes
    test('DELETE /api/userlist/:email/:friendRequest/decline should return an error for a non-existing friend request', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request
        .delete(`/api/userlist/${userEmail}/${friendEmail}/decline`);
  
      expect(res.status).toBe(400);
      expect(res.body.error).toBe("Friend request not found in the user's friend requests");
    });

  });
  


  
  






