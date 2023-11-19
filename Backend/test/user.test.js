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

const userEmail = 'koltonluu@gmail.com';
const nonExistingEmail = 'nonexistinguser@example.com';


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
  

// Interface POST https://20.163.28.92:8081/api/userlist
describe('Create a new user', () => {
    // Input: user is a new correct user
    // Expected status code: 201
    // Expected behavior: user is added to the database
    // Expected output: 'User created successfully'
  test('POST /api/userlist should create a new user', async () => {
    const res = await request(app)
      .post('/api/userlist')
      .send(user);

    expect(res.status).toBe(201);
    expect(res.body.message).toBe('User created successfully');
  });

    // Input: user is an existing correct user with the same email in the database
    // Expected status code: 409
    // Expected behavior: database is unchanged
    // Expected output: 'User with this email already exists'
  test('POST /api/userlist should return an error if the user already exists', async () => {
    const res = await request(app)
      .post('/api/userlist')
      .send(user);

    expect(res.status).toBe(409);
    expect(res.body.message).toBe('User with this email already exists');
  });
   // Test for handling server errors during user creation
   test('POST /api/userlist should handle server errors during user creation', async () => {
    // Mocking an error during the database insertion
    const collectionMock = jest.spyOn(client.db('UserDB').collection('userlist'), 'insertOne');
    collectionMock.mockImplementationOnce(() => {
      throw new Error('Mocked MongoDB insertion error');
    });

    try {
      const res = await request(app)
        .post('/api/userlist')
        .send(user);

      console.log('Response:', res.status, res.body);

      expect(res.status).toBe(500);
      expect(res.body.error).toBe('Internal server error');
    } catch (error) {
      console.error('Test error:', error);
    } finally {
      // Restore the original implementation of the mocked method
      collectionMock.mockRestore();
    }
  });
  
});

// Interface GET https://20.163.28.92:8081/api/userlist/:email
describe('Get an email', () => {
    // Test for getting an existing user by email
    test('GET /api/userlist/:email should retrieve an existing user by email', async () => {
      // Assuming userEmail is a valid email that exists in the database
      const res = await request(app)
        .get(`/api/userlist/${userEmail}`);
  
      expect(res.status).toBe(200);
      expect(res.body.email).toBe(userEmail); // Assuming user email is returned in the response
    });
  
    // Test for getting a non-existing user by email
    test('GET /api/userlist/:email should return an error for a non-existing user', async () => {
      // Assuming nonExistingEmail is not present in the database
      const res = await request(app)
        .get(`/api/userlist/${nonExistingEmail}`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User not found');
    });
  
    
    // Test for handling server errors during user retrieval
    test('GET /api/userlist/:email should handle server errors during retrieval', async () => {
        // Mocking an error during the database query
        const collectionMock = jest.spyOn(client.db('UserDB').collection('userlist'), 'findOne');
        collectionMock.mockImplementationOnce(() => {
          throw new Error('Mocked MongoDB query error');
        });
      
        try {
          const res = await request(app)
            .get(`/api/userlist/${userEmail}`);
      
          console.log('Response:', res.status, res.body);
      
          expect(res.status).toBe(500);
          expect(res.body.error).toBe('Internal server error');
        } catch (error) {
          console.error('Test error:', error);
        } finally {
          // Restore the original implementation of the mocked method
          collectionMock.mockRestore();
        }
      });
    
  });
  

  describe('Update User Address', () => {
    // Test for successfully updating a user's address
    test('PUT /api/userlist/:email/address should update user address successfully', async () => {
      // Assuming userEmail is a valid email that exists in the database
      const newAddress = '123 Main St';
  
      const res = await request(app)
        .put(`/api/userlist/${userEmail}/address`)
        .send({ address: newAddress });
  
      expect(res.status).toBe(200);
      expect(res.body.message).toBe('User address updated successfully');
    });
  
    // Test for updating the address of a non-existing user
    test('PUT /api/userlist/:email/address should return an error for a non-existing user', async () => {
      // Assuming nonExistingEmail is not present in the database
      const newAddress = '456 Oak St';
  
      const res = await request(app)
        .put(`/api/userlist/${nonExistingEmail}/address`)
        .send({ address: newAddress });
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User not found');
    });
  
    // Test for handling server errors during address update
    test('PUT /api/userlist/:email/address should handle server errors during update', async () => {
      // Mocking an error during the database update
      const collectionMock = jest.spyOn(client.db('UserDB').collection('userlist'), 'updateOne');
      collectionMock.mockImplementationOnce(() => {
        throw new Error('Mocked MongoDB update error');
      });
  
      try {
        const newAddress = '789 Pine St';
  
        const res = await request(app)
          .put(`/api/userlist/${userEmail}/address`)
          .send({ address: newAddress });
  
        console.log('Response:', res.status, res.body);
  
        expect(res.status).toBe(500);
        expect(res.body.error).toBe('Internal server error');
      } catch (error) {
        console.error('Test error:', error);
      } finally {
        // Restore the original implementation of the mocked method
        collectionMock.mockRestore();
      }
    });
  });

  describe('Get User Friend List with Names', () => {
    // Test for successfully retrieving user friend list with names
    test('GET /api/userlist/:email/friends should retrieve user friend list with names', async () => {
      // Assuming userEmail is a valid email that exists in the database
      const res = await request(app)
        .get(`/api/userlist/${userEmail}/friends`);
  
      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('friendsWithNames');
      expect(res.body).toHaveProperty('friendRequestsWithNames');
      expect(Array.isArray(res.body.friendsWithNames)).toBe(true);
      expect(Array.isArray(res.body.friendRequestsWithNames)).toBe(true);
    });
  
    // Test for retrieving friend list with names for a non-existing user
    test('GET /api/userlist/:email/friends should return an error for a non-existing user', async () => {
      // Assuming nonExistingEmail is not present in the database
      const res = await request(app)
        .get(`/api/userlist/${nonExistingEmail}/friends`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User not found');
    });
  
    // Test for handling server errors during friend list retrieval
    test('GET /api/userlist/:email/friends should handle server errors during retrieval', async () => {
      // Mocking an error during the database query
      const collectionMock = jest.spyOn(client.db('UserDB').collection('userlist'), 'findOne');
      collectionMock.mockImplementationOnce(() => {
        throw new Error('Mocked MongoDB query error');
      });
  
      try {
        const res = await request(app)
          .get(`/api/userlist/${userEmail}/friends`);
  
        console.log('Response:', res.status, res.body);
  
        expect(res.status).toBe(500);
        expect(res.body.error).toBe('Internal server error');
      } catch (error) {
        console.error('Test error:', error);
      } finally {
        // Restore the original implementation of the mocked method
        collectionMock.mockRestore();
      }
    });
  });

  describe('Send Friend Request', () => {
    // Test for successfully sending a friend request
    test('POST /api/userlist/:email/friendRequest should send a friend request successfully', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
      const friendEmail = 'friend1@example.com';
  
      const res = await request(app)
        .post(`/api/userlist/${userEmail}/friendRequest`)
        .send({ email: friendEmail });
  
      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Friend request sent successfully');
    });
  
    // Test for sending a friend request to a non-existing user
    test('POST /api/userlist/:email/friendRequest should return an error for a non-existing user', async () => {
      // Assuming nonExistingEmail is not present in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request(app)
        .post(`/api/userlist/${nonExistingEmail}/friendRequest`)
        .send({ email: friendEmail });
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User not found');
    });
  
    // Test for sending a friend request to a non-existing friend
    test('POST /api/userlist/:email/friendRequest should return an error for a non-existing friend', async () => {
      // Assuming userEmail is a valid email that exists in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request(app)
        .post(`/api/userlist/${userEmail}/friendRequest`)
        .send({ email: friendEmail });
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Friend not found in the userlist');
    });
  
    // Test for sending a friend request to an existing friend
    test('POST /api/userlist/:email/friendRequest should return an error for an existing friend', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
      const friendEmail = 'friend@example.com';
  
      /*
      // Assuming the friend request has already been sent
      await request(app)
        .post(`/api/userlist/${userEmail}/friendRequest`)
        .send({ email: friendEmail });
  
    */

      // Sending the friend request again
      const res = await request(app)
        .post(`/api/userlist/${userEmail}/friendRequest`)
        .send({ email: friendEmail });
  
      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Friend request already sent');
    });
  
    // Test for handling server errors during friend request
    test('POST /api/userlist/:email/friendRequest should handle server errors during request', async () => {
      // Mocking an error during the database update
      const collectionMock = jest.spyOn(client.db('UserDB').collection('userlist'), 'updateOne');
      collectionMock.mockImplementationOnce(() => {
        throw new Error('Mocked MongoDB update error');
      });
  
      try {
        const friendEmail = 'friend@example.com';
  
        const res = await request(app)
          .post(`/api/userlist/${userEmail}/friendRequest`)
          .send({ email: friendEmail });
  
        console.log('Response:', res.status, res.body);
  
        expect(res.status).toBe(500);
        expect(res.body.error).toBe('Internal server error');
      } catch (error) {
        console.error('Test error:', error);
      } finally {
        // Restore the original implementation of the mocked method
        collectionMock.mockRestore();
      }
    });
  });

  describe('Accept Friend Request', () => {
    // Test for successfully accepting a friend request
    test('POST /api/userlist/:email/:friendRequest/accept should accept a friend request successfully', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
      const friendEmail = 'friend@example.com';
  
      // Assuming a friend request has been sent
      await request(app)
        .post(`/api/userlist/${friendEmail}/friendRequest`)
        .send({ email: userEmail });
  
      const res = await request(app)
        .post(`/api/userlist/${userEmail}/${friendEmail}/accept`);
  
      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Friend request accepted successfully');
    });
  
    // Test for accepting a friend request from a non-existing user
    test('POST /api/userlist/:email/:friendRequest/accept should return an error for a non-existing user', async () => {
      // Assuming userEmail is a valid email that exists in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request(app)
        .post(`/api/userlist/${nonExistingEmail}/${friendEmail}/accept`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User not found');
    });
  
    // Test for accepting a friend request that doesn't exist in the user's friend requests
    test('POST /api/userlist/:email/:friendRequest/accept should return an error for a non-existing friend request', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request(app)
        .post(`/api/userlist/${userEmail}/${friendEmail}/accept`);
  
      expect(res.status).toBe(400);
      expect(res.body.error).toBe("Friend request not found in the user's friend requests");
    });
  
    // Test for handling server errors during friend request acceptance
    test('POST /api/userlist/:email/:friendRequest/accept should handle server errors during acceptance', async () => {
      // Mocking an error during the database update
      const collectionMock = jest.spyOn(client.db('UserDB').collection('userlist'), 'updateOne');
      collectionMock.mockImplementationOnce(() => {
        throw new Error('Mocked MongoDB update error');
      });
  
      try {
        // Assuming userEmail and friendEmail are valid emails that exist in the database
        const friendEmail = 'friend@example.com';
  
        const res = await request(app)
          .post(`/api/userlist/${userEmail}/${friendEmail}/accept`);
  
        console.log('Response:', res.status, res.body);
  
        expect(res.status).toBe(500);
        expect(res.body.error).toBe('Internal server error');
      } catch (error) {
        console.error('Test error:', error);
      } finally {
        // Restore the original implementation of the mocked method
        collectionMock.mockRestore();
      }
    });
  });

  
  describe('Decline Friend Request', () => {
    // Test for successfully declining a friend request
    test('DELETE /api/userlist/:email/:friendRequest/decline should decline a friend request successfully', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
      const friendEmail = 'friend4@example.com';
  
      // Assuming a friend request has been sent
      await request(app)
        .post(`/api/userlist/${friendEmail}/friendRequest`)
        .send({ email: userEmail });
  
      const res = await request(app)
        .delete(`/api/userlist/${userEmail}/${friendEmail}/decline`);
  
      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Friend request declined successfully');
    });
  
    // Test for declining a friend request from a non-existing user
    test('DELETE /api/userlist/:email/:friendRequest/decline should return an error for a non-existing user', async () => {
      // Assuming userEmail is a valid email that exists in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request(app)
        .delete(`/api/userlist/${nonExistingEmail}/${friendEmail}/decline`);
  
      expect(res.status).toBe(404);
      expect(res.body.error).toBe('User not found');
    });
  
    // Test for declining a friend request that doesn't exist in the user's friend requests
    test('DELETE /api/userlist/:email/:friendRequest/decline should return an error for a non-existing friend request', async () => {
      // Assuming userEmail and friendEmail are valid emails that exist in the database
      const friendEmail = 'nonexistingfriend@example.com';
  
      const res = await request(app)
        .delete(`/api/userlist/${userEmail}/${friendEmail}/decline`);
  
      expect(res.status).toBe(400);
      expect(res.body.error).toBe("Friend request not found in the user's friend requests");
    });
  
    // Test for handling server errors during friend request decline
    test('DELETE /api/userlist/:email/:friendRequest/decline should handle server errors during decline', async () => {
      // Mocking an error during the database update
      const collectionMock = jest.spyOn(client.db('UserDB').collection('userlist'), 'updateOne');
      collectionMock.mockImplementationOnce(() => {
        throw new Error('Mocked MongoDB update error');
      });
  
      try {
        // Assuming userEmail and friendEmail are valid emails that exist in the database
        const friendEmail = 'friend@example.com';
  
        const res = await request(app)
          .delete(`/api/userlist/${userEmail}/${friendEmail}/decline`);
  
        console.log('Response:', res.status, res.body);
  
        expect(res.status).toBe(500);
        expect(res.body.error).toBe('Internal server error');
      } catch (error) {
        console.error('Test error:', error);
      } finally {
        // Restore the original implementation of the mocked method
        collectionMock.mockRestore();
      }
    });
  });
  


  
  






