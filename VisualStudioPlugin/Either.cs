using System;
using System.Threading.Tasks;

namespace DSLPlatform
{
	public struct Either<T>
	{
		public bool Success { get { return Error == null; } }
		public string Error;
		public T Value;

		public static Either<T> Fail(string msg) { return new Either<T> { Error = msg }; }
	}

	public static class Either
	{
		public static Either<T> Success<T>(T value) { return new Either<T> { Value = value }; }

		public static void OnSuccess<T>(this Task<Either<T>> task, Action<T> continueWith)
		{
			task.ContinueWith(t =>
			{
				if (t.Exception == null && t.Result.Success)
					continueWith(t.Result.Value);
			});
		}
	}

}
