using System;
using System.Collections.Generic;
using System.IO;

namespace DSLPlatform
{
	internal class ChunkedMemoryStream : Stream
	{
		private List<byte[]> Blocks = new List<byte[]>();
		private int CurrentPosition;
		private int TotalSize;
		private const int BlockSize = 65536;

		public ChunkedMemoryStream()
		{
			Blocks.Add(new byte[BlockSize]);
		}

		public ChunkedMemoryStream(Stream stream)
			: this()
		{
			stream.CopyTo(this);
			CurrentPosition = 0;
		}

		public override bool CanRead { get { return true; } }
		public override bool CanSeek { get { return true; } }
		public override bool CanWrite { get { return true; } }
		public override void Flush() { }
		public override long Length { get { return TotalSize; } }
		public override long Position
		{
			get { return CurrentPosition; }
			set { CurrentPosition = (int)value; }
		}
		public override int Read(byte[] buffer, int offset, int count)
		{
			var off = CurrentPosition % BlockSize;
			var min = BlockSize - off;
			if (count < min)
				min = count;
			if (TotalSize - CurrentPosition < min)
				min = TotalSize - CurrentPosition;
			if (min > 0)
			{
				var pos = CurrentPosition / BlockSize;
				Buffer.BlockCopy(Blocks[pos], off, buffer, offset, min);
				CurrentPosition += min;
			}
			return min;
		}
		public override long Seek(long offset, SeekOrigin origin)
		{
			switch (origin)
			{
				case SeekOrigin.Begin:
					CurrentPosition = (int)offset;
					break;
				case SeekOrigin.Current:
					CurrentPosition += (int)offset;
					break;
				default:
					CurrentPosition = TotalSize - (int)offset;
					break;
			}
			return CurrentPosition;
		}
		public override void SetLength(long value)
		{
			TotalSize = (int)value;
			if (CurrentPosition > TotalSize)
				CurrentPosition = TotalSize;
		}
		public override void WriteByte(byte value)
		{
			var off = CurrentPosition % BlockSize;
			var pos = CurrentPosition / BlockSize;
			Blocks[pos][off] = value;
			CurrentPosition += 1;
			if (BlockSize == off + 1 && Blocks.Count == pos + 1)
				Blocks.Add(new byte[BlockSize]);
			if (CurrentPosition > TotalSize)
				TotalSize = CurrentPosition;
		}
		public override void Write(byte[] buffer, int offset, int count)
		{
			int cur = count;
			while (cur > 0)
			{
				var off = CurrentPosition % BlockSize;
				var pos = CurrentPosition / BlockSize;
				var min = BlockSize - off;
				if (cur < min)
					min = cur;
				Buffer.BlockCopy(buffer, offset + count - cur, Blocks[pos], off, min);
				cur -= min;
				CurrentPosition += min;
				if (min == BlockSize - off && Blocks.Count == pos + 1)
					Blocks.Add(new byte[BlockSize]);
			}
			if (CurrentPosition > TotalSize)
				TotalSize = CurrentPosition;
		}
		bool disposed;

		protected override void Dispose(bool disposing)
		{
			base.Dispose(disposing);
			if (disposing && !disposed)
			{
				disposed = true;
				CurrentPosition = 0;
				TotalSize = 0;
			}
		}
	}
}