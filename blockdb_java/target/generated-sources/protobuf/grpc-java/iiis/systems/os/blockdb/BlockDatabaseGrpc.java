package iiis.systems.os.blockdb;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 * <pre>
 * Interface exported by the server.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.2.0)",
    comments = "Source: db.proto")
public final class BlockDatabaseGrpc {

  private BlockDatabaseGrpc() {}

  public static final String SERVICE_NAME = "blockdb.BlockDatabase";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.GetRequest,
      iiis.systems.os.blockdb.GetResponse> METHOD_GET =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "Get"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.GetRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.GetResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.Transaction,
      iiis.systems.os.blockdb.BooleanResponse> METHOD_TRANSFER =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "Transfer"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Transaction.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.BooleanResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.Transaction,
      iiis.systems.os.blockdb.VerifyResponse> METHOD_VERIFY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "Verify"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Transaction.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.VerifyResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.Null,
      iiis.systems.os.blockdb.GetHeightResponse> METHOD_GET_HEIGHT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "GetHeight"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Null.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.GetHeightResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.GetBlockRequest,
      iiis.systems.os.blockdb.JsonBlockString> METHOD_GET_BLOCK =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "GetBlock"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.GetBlockRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.JsonBlockString.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.JsonBlockString,
      iiis.systems.os.blockdb.Null> METHOD_PUSH_BLOCK =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "PushBlock"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.JsonBlockString.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Null.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.Transaction,
      iiis.systems.os.blockdb.Null> METHOD_PUSH_TRANSACTION =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "PushTransaction"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Transaction.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Null.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BlockDatabaseStub newStub(io.grpc.Channel channel) {
    return new BlockDatabaseStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BlockDatabaseBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new BlockDatabaseBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static BlockDatabaseFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new BlockDatabaseFutureStub(channel);
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static abstract class BlockDatabaseImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Return UserID's Balance on the Chain, after considering the latest valid block. Pending transactions have no effect on Get()
     * </pre>
     */
    public void get(iiis.systems.os.blockdb.GetRequest request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET, responseObserver);
    }

    /**
     * <pre>
     * Receive and Broadcast Transaction: balance[FromID]-=Value, balance[ToID]+=(Value-MiningFee), balance[MinerID]+=MiningFee
     * Return Success=false if FromID is same as ToID or latest balance of FromID is insufficient
     * </pre>
     */
    public void transfer(iiis.systems.os.blockdb.Transaction request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_TRANSFER, responseObserver);
    }

    /**
     * <pre>
     * Check if a transaction has been written into a block, or is still waiting, or is invalid on the longest branch.
     * </pre>
     */
    public void verify(iiis.systems.os.blockdb.Transaction request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.VerifyResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_VERIFY, responseObserver);
    }

    /**
     * <pre>
     * Get the current blockchain length; use the longest branch if multiple branch exist.
     * </pre>
     */
    public void getHeight(iiis.systems.os.blockdb.Null request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetHeightResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_HEIGHT, responseObserver);
    }

    /**
     * <pre>
     * Get the Json representation of the block with BlockHash hash value
     * </pre>
     */
    public void getBlock(iiis.systems.os.blockdb.GetBlockRequest request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.JsonBlockString> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_BLOCK, responseObserver);
    }

    /**
     * <pre>
     * Send a block to another server
     * </pre>
     */
    public void pushBlock(iiis.systems.os.blockdb.JsonBlockString request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.Null> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PUSH_BLOCK, responseObserver);
    }

    /**
     * <pre>
     * Send a transaction to another server
     * </pre>
     */
    public void pushTransaction(iiis.systems.os.blockdb.Transaction request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.Null> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PUSH_TRANSACTION, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_GET,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.GetRequest,
                iiis.systems.os.blockdb.GetResponse>(
                  this, METHODID_GET)))
          .addMethod(
            METHOD_TRANSFER,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.Transaction,
                iiis.systems.os.blockdb.BooleanResponse>(
                  this, METHODID_TRANSFER)))
          .addMethod(
            METHOD_VERIFY,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.Transaction,
                iiis.systems.os.blockdb.VerifyResponse>(
                  this, METHODID_VERIFY)))
          .addMethod(
            METHOD_GET_HEIGHT,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.Null,
                iiis.systems.os.blockdb.GetHeightResponse>(
                  this, METHODID_GET_HEIGHT)))
          .addMethod(
            METHOD_GET_BLOCK,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.GetBlockRequest,
                iiis.systems.os.blockdb.JsonBlockString>(
                  this, METHODID_GET_BLOCK)))
          .addMethod(
            METHOD_PUSH_BLOCK,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.JsonBlockString,
                iiis.systems.os.blockdb.Null>(
                  this, METHODID_PUSH_BLOCK)))
          .addMethod(
            METHOD_PUSH_TRANSACTION,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.Transaction,
                iiis.systems.os.blockdb.Null>(
                  this, METHODID_PUSH_TRANSACTION)))
          .build();
    }
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static final class BlockDatabaseStub extends io.grpc.stub.AbstractStub<BlockDatabaseStub> {
    private BlockDatabaseStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BlockDatabaseStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BlockDatabaseStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BlockDatabaseStub(channel, callOptions);
    }

    /**
     * <pre>
     * Return UserID's Balance on the Chain, after considering the latest valid block. Pending transactions have no effect on Get()
     * </pre>
     */
    public void get(iiis.systems.os.blockdb.GetRequest request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Receive and Broadcast Transaction: balance[FromID]-=Value, balance[ToID]+=(Value-MiningFee), balance[MinerID]+=MiningFee
     * Return Success=false if FromID is same as ToID or latest balance of FromID is insufficient
     * </pre>
     */
    public void transfer(iiis.systems.os.blockdb.Transaction request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_TRANSFER, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Check if a transaction has been written into a block, or is still waiting, or is invalid on the longest branch.
     * </pre>
     */
    public void verify(iiis.systems.os.blockdb.Transaction request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.VerifyResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_VERIFY, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the current blockchain length; use the longest branch if multiple branch exist.
     * </pre>
     */
    public void getHeight(iiis.systems.os.blockdb.Null request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetHeightResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_HEIGHT, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the Json representation of the block with BlockHash hash value
     * </pre>
     */
    public void getBlock(iiis.systems.os.blockdb.GetBlockRequest request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.JsonBlockString> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_BLOCK, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Send a block to another server
     * </pre>
     */
    public void pushBlock(iiis.systems.os.blockdb.JsonBlockString request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.Null> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PUSH_BLOCK, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Send a transaction to another server
     * </pre>
     */
    public void pushTransaction(iiis.systems.os.blockdb.Transaction request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.Null> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PUSH_TRANSACTION, getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static final class BlockDatabaseBlockingStub extends io.grpc.stub.AbstractStub<BlockDatabaseBlockingStub> {
    private BlockDatabaseBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BlockDatabaseBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BlockDatabaseBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BlockDatabaseBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Return UserID's Balance on the Chain, after considering the latest valid block. Pending transactions have no effect on Get()
     * </pre>
     */
    public iiis.systems.os.blockdb.GetResponse get(iiis.systems.os.blockdb.GetRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET, getCallOptions(), request);
    }

    /**
     * <pre>
     * Receive and Broadcast Transaction: balance[FromID]-=Value, balance[ToID]+=(Value-MiningFee), balance[MinerID]+=MiningFee
     * Return Success=false if FromID is same as ToID or latest balance of FromID is insufficient
     * </pre>
     */
    public iiis.systems.os.blockdb.BooleanResponse transfer(iiis.systems.os.blockdb.Transaction request) {
      return blockingUnaryCall(
          getChannel(), METHOD_TRANSFER, getCallOptions(), request);
    }

    /**
     * <pre>
     * Check if a transaction has been written into a block, or is still waiting, or is invalid on the longest branch.
     * </pre>
     */
    public iiis.systems.os.blockdb.VerifyResponse verify(iiis.systems.os.blockdb.Transaction request) {
      return blockingUnaryCall(
          getChannel(), METHOD_VERIFY, getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the current blockchain length; use the longest branch if multiple branch exist.
     * </pre>
     */
    public iiis.systems.os.blockdb.GetHeightResponse getHeight(iiis.systems.os.blockdb.Null request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_HEIGHT, getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the Json representation of the block with BlockHash hash value
     * </pre>
     */
    public iiis.systems.os.blockdb.JsonBlockString getBlock(iiis.systems.os.blockdb.GetBlockRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_BLOCK, getCallOptions(), request);
    }

    /**
     * <pre>
     * Send a block to another server
     * </pre>
     */
    public iiis.systems.os.blockdb.Null pushBlock(iiis.systems.os.blockdb.JsonBlockString request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PUSH_BLOCK, getCallOptions(), request);
    }

    /**
     * <pre>
     * Send a transaction to another server
     * </pre>
     */
    public iiis.systems.os.blockdb.Null pushTransaction(iiis.systems.os.blockdb.Transaction request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PUSH_TRANSACTION, getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static final class BlockDatabaseFutureStub extends io.grpc.stub.AbstractStub<BlockDatabaseFutureStub> {
    private BlockDatabaseFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BlockDatabaseFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BlockDatabaseFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BlockDatabaseFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Return UserID's Balance on the Chain, after considering the latest valid block. Pending transactions have no effect on Get()
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.GetResponse> get(
        iiis.systems.os.blockdb.GetRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET, getCallOptions()), request);
    }

    /**
     * <pre>
     * Receive and Broadcast Transaction: balance[FromID]-=Value, balance[ToID]+=(Value-MiningFee), balance[MinerID]+=MiningFee
     * Return Success=false if FromID is same as ToID or latest balance of FromID is insufficient
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.BooleanResponse> transfer(
        iiis.systems.os.blockdb.Transaction request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_TRANSFER, getCallOptions()), request);
    }

    /**
     * <pre>
     * Check if a transaction has been written into a block, or is still waiting, or is invalid on the longest branch.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.VerifyResponse> verify(
        iiis.systems.os.blockdb.Transaction request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_VERIFY, getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the current blockchain length; use the longest branch if multiple branch exist.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.GetHeightResponse> getHeight(
        iiis.systems.os.blockdb.Null request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_HEIGHT, getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the Json representation of the block with BlockHash hash value
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.JsonBlockString> getBlock(
        iiis.systems.os.blockdb.GetBlockRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_BLOCK, getCallOptions()), request);
    }

    /**
     * <pre>
     * Send a block to another server
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.Null> pushBlock(
        iiis.systems.os.blockdb.JsonBlockString request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PUSH_BLOCK, getCallOptions()), request);
    }

    /**
     * <pre>
     * Send a transaction to another server
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.Null> pushTransaction(
        iiis.systems.os.blockdb.Transaction request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PUSH_TRANSACTION, getCallOptions()), request);
    }
  }

  private static final int METHODID_GET = 0;
  private static final int METHODID_TRANSFER = 1;
  private static final int METHODID_VERIFY = 2;
  private static final int METHODID_GET_HEIGHT = 3;
  private static final int METHODID_GET_BLOCK = 4;
  private static final int METHODID_PUSH_BLOCK = 5;
  private static final int METHODID_PUSH_TRANSACTION = 6;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BlockDatabaseImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BlockDatabaseImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET:
          serviceImpl.get((iiis.systems.os.blockdb.GetRequest) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetResponse>) responseObserver);
          break;
        case METHODID_TRANSFER:
          serviceImpl.transfer((iiis.systems.os.blockdb.Transaction) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse>) responseObserver);
          break;
        case METHODID_VERIFY:
          serviceImpl.verify((iiis.systems.os.blockdb.Transaction) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.VerifyResponse>) responseObserver);
          break;
        case METHODID_GET_HEIGHT:
          serviceImpl.getHeight((iiis.systems.os.blockdb.Null) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetHeightResponse>) responseObserver);
          break;
        case METHODID_GET_BLOCK:
          serviceImpl.getBlock((iiis.systems.os.blockdb.GetBlockRequest) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.JsonBlockString>) responseObserver);
          break;
        case METHODID_PUSH_BLOCK:
          serviceImpl.pushBlock((iiis.systems.os.blockdb.JsonBlockString) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.Null>) responseObserver);
          break;
        case METHODID_PUSH_TRANSACTION:
          serviceImpl.pushTransaction((iiis.systems.os.blockdb.Transaction) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.Null>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class BlockDatabaseDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return iiis.systems.os.blockdb.DBProto.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (BlockDatabaseGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BlockDatabaseDescriptorSupplier())
              .addMethod(METHOD_GET)
              .addMethod(METHOD_TRANSFER)
              .addMethod(METHOD_VERIFY)
              .addMethod(METHOD_GET_HEIGHT)
              .addMethod(METHOD_GET_BLOCK)
              .addMethod(METHOD_PUSH_BLOCK)
              .addMethod(METHOD_PUSH_TRANSACTION)
              .build();
        }
      }
    }
    return result;
  }
}
